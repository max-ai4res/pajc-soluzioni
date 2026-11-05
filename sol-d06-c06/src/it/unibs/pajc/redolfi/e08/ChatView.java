package it.unibs.pajc.redolfi.e08;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * La finestra di WhatsPAJC: l'elenco degli utenti a sinistra, la conversazione a destra,
 * il campo di testo in basso, la riga di stato. Ascolta il ChatModel e si aggiorna; non
 * sa nulla di socket. Le azioni dell'utente le consegna al Controller con tre callback.
 */
public class ChatView extends JFrame {

    private static final long serialVersionUID = 1L;

    private final ChatModel model;
    private final DefaultListModel<String> utenti = new DefaultListModel<>();
    private final JList<String> lstUtenti = new JList<>(utenti);
    private final JTextPane txtConversazione = new JTextPane();
    private final JTextField txtMessaggio = new JTextField();
    private final JButton btnInvia = new JButton("Invia");
    private final JButton btnConnetti = new JButton("Connetti");
    private final JLabel lblStato = new JLabel("Non connesso");

    private final SimpleAttributeSet stilePubblico = new SimpleAttributeSet();
    private final SimpleAttributeSet stilePrivato = new SimpleAttributeSet();
    private final SimpleAttributeSet stileSistema = new SimpleAttributeSet();
    private final SimpleAttributeSet stileNome = new SimpleAttributeSet();

    public ChatView(ChatModel model) {
        super("WhatsPAJC");
        this.model = model;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        StyleConstants.setForeground(stilePrivato, new Color(0x8B, 0x2E, 0x8B));
        StyleConstants.setItalic(stilePrivato, true);
        StyleConstants.setForeground(stileSistema, new Color(0x6B, 0x75, 0x85));
        StyleConstants.setBold(stileNome, true);

        // a sinistra gli utenti, a destra la conversazione
        lstUtenti.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        JScrollPane scrUtenti = new JScrollPane(lstUtenti);
        scrUtenti.setPreferredSize(new Dimension(150, 0));
        scrUtenti.setBorder(BorderFactory.createTitledBorder("Utenti"));
        txtConversazione.setEditable(false);
        txtConversazione.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        JScrollPane scrConversazione = new JScrollPane(txtConversazione);
        scrConversazione.setBorder(BorderFactory.createTitledBorder("Conversazione"));
        JSplitPane centro = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrUtenti, scrConversazione);
        centro.setBorder(null);

        // in basso il campo di testo e il pulsante
        JPanel sud = new JPanel(new BorderLayout(6, 0));
        sud.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        sud.add(txtMessaggio, BorderLayout.CENTER);
        sud.add(btnInvia, BorderLayout.EAST);

        // in alto la barra con Connetti e la riga di stato
        JPanel nord = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        nord.add(btnConnetti);
        nord.add(lblStato);

        JPanel contenuto = new JPanel(new BorderLayout());
        contenuto.add(nord, BorderLayout.NORTH);
        contenuto.add(centro, BorderLayout.CENTER);
        contenuto.add(sud, BorderLayout.SOUTH);
        setContentPane(contenuto);

        model.addPropertyChangeListener(this::modelCambiato);
        aggiornaStato();
        setSize(640, 420);
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------ il Model avvisa

    private void modelCambiato(PropertyChangeEvent e) {
        switch (e.getPropertyName()) {
            case "stato", "nome" -> aggiornaStato();
            case "utenti" -> {
                utenti.clear();
                utenti.addAll(model.utenti());
            }
            case "riga" -> aggiungiRiga((ChatModel.Riga) e.getNewValue());
            case "errore" -> JOptionPane.showMessageDialog(this, e.getNewValue(),
                    "WhatsPAJC", JOptionPane.ERROR_MESSAGE);
            default -> { }
        }
    }

    private void aggiornaStato() {
        boolean connesso = model.stato() == ChatModel.Stato.CONNESSO;
        lblStato.setText(switch (model.stato()) {
            case DISCONNESSO -> "Non connesso";
            case IN_CONNESSIONE -> "Connessione in corso...";
            case CONNESSO -> model.nome() == null ? "Connesso, scegli un nome"
                                                  : "Connesso come " + model.nome();
        });
        btnConnetti.setText(model.stato() == ChatModel.Stato.DISCONNESSO ? "Connetti" : "Disconnetti");
        btnConnetti.setEnabled(model.stato() != ChatModel.Stato.IN_CONNESSIONE);
        txtMessaggio.setEnabled(connesso);
        btnInvia.setEnabled(connesso);
        if (!connesso) {
            utenti.clear();
            if (model.stato() == ChatModel.Stato.DISCONNESSO) txtConversazione.setText("");
        }
    }

    private void aggiungiRiga(ChatModel.Riga riga) {
        StyledDocument doc = txtConversazione.getStyledDocument();
        try {
            switch (riga.tipo()) {
                case PUBBLICO -> {
                    doc.insertString(doc.getLength(), riga.da() + ": ", stileNome);
                    doc.insertString(doc.getLength(), riga.testo() + "\n", stilePubblico);
                }
                case PRIVATO -> {
                    doc.insertString(doc.getLength(), riga.da() + " → " + riga.a() + ": ", stileNome);
                    doc.insertString(doc.getLength(), riga.testo() + "\n", stilePrivato);
                }
                case SISTEMA -> doc.insertString(doc.getLength(), riga.testo() + "\n", stileSistema);
            }
        } catch (BadLocationException e) {
            throw new IllegalStateException(e);
        }
        txtConversazione.setCaretPosition(doc.getLength());
    }

    // ------------------------------------------------------------ l'utente agisce

    /** Il Controller si registra qui: che cosa fare con il testo scritto. */
    public void onInvia(Consumer<String> azione) {
        ActionListener l = e -> {
            String testo = txtMessaggio.getText().strip();
            if (!testo.isEmpty()) {
                azione.accept(testo);
                txtMessaggio.setText("");
            }
        };
        txtMessaggio.addActionListener(l);          // Invio nel campo
        btnInvia.addActionListener(l);
    }

    public void onConnetti(Runnable azione) {
        btnConnetti.addActionListener(e -> azione.run());
    }

    /** Doppio click su un utente: il campo si prepara a un privato. */
    public void onUtenteScelto(Consumer<String> azione) {
        lstUtenti.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && lstUtenti.getSelectedValue() != null)
                    azione.accept(lstUtenti.getSelectedValue());
            }
        });
    }

    public void preparaPrivato(String utente) {
        txtMessaggio.setText("/a " + utente + " ");
        txtMessaggio.requestFocusInWindow();
    }

    public void selezionaUtente(String utente) {
        lstUtenti.setSelectedValue(utente, true);
    }
}
