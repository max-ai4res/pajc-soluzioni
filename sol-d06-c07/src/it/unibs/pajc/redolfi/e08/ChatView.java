package it.unibs.pajc.redolfi.e08;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;
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
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * La View di WhatsPAJC completa: le schede delle conversazioni con i non letti, la
 * tendina delle stanze, il pulsante Foto, le foto ricevute in un dialogo. Si rifa' dal
 * Model e consegna le azioni dell'utente al Controller come callback.
 */
public class ChatView extends JFrame {

    private static final long serialVersionUID = 1L;

    private final ChatModel model;
    private final DefaultListModel<String> utenti = new DefaultListModel<>();
    private final JList<String> lstUtenti = new JList<>(utenti);
    private final JTabbedPane schede = new JTabbedPane();
    private final Map<String, JTextPane> pannelli = new HashMap<>();
    private final JTextField txtMessaggio = new JTextField();
    private final JButton btnInvia = new JButton("Invia");
    private final JButton btnConnetti = new JButton("Connetti");
    private final JButton btnFoto = new JButton("Foto...");
    private final javax.swing.JComboBox<String> cmbStanze = new javax.swing.JComboBox<>();
    private boolean aggiornandoStanze;
    private final JLabel lblStato = new JLabel("Non connesso");
    private final SimpleAttributeSet stileNome = new SimpleAttributeSet();
    private final SimpleAttributeSet stilePrivato = new SimpleAttributeSet();
    private final SimpleAttributeSet stileSistema = new SimpleAttributeSet();
    private Consumer<String> onScheda = s -> { };

    public ChatView(ChatModel model) {
        super("WhatsPAJC");
        this.model = model;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        StyleConstants.setBold(stileNome, true);
        StyleConstants.setItalic(stilePrivato, true);
        StyleConstants.setForeground(stilePrivato, new Color(0x8B, 0x2E, 0x8B));
        StyleConstants.setForeground(stileSistema, new Color(0x6B, 0x75, 0x85));

        JScrollPane scrUtenti = new JScrollPane(lstUtenti);
        scrUtenti.setPreferredSize(new Dimension(150, 0));
        scrUtenti.setBorder(BorderFactory.createTitledBorder("Utenti"));
        JPanel sud = new JPanel(new BorderLayout(6, 0));
        sud.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        sud.add(txtMessaggio, BorderLayout.CENTER);
        sud.add(btnInvia, BorderLayout.EAST);
        JPanel nord = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        nord.add(btnConnetti);
        nord.add(btnFoto);
        nord.add(new JLabel("Stanza:"));
        nord.add(cmbStanze);
        nord.add(lblStato);
        cmbStanze.setPrototypeDisplayValue("generale (10)");
        add(nord, BorderLayout.NORTH);
        add(scrUtenti, BorderLayout.WEST);
        add(schede, BorderLayout.CENTER);
        add(sud, BorderLayout.SOUTH);

        schede.addChangeListener(e -> {
            int i = schede.getSelectedIndex();
            if (i >= 0) onScheda.accept(nomeScheda(i));
        });
        model.addPropertyChangeListener(this::modelCambiato);
        scheda(ChatModel.TUTTI);
        aggiornaStato();
        setSize(680, 440);
        setLocationRelativeTo(null);
    }

    private String nomeScheda(int i) {
        String t = schede.getTitleAt(i);
        int p = t.indexOf(" (");
        return p < 0 ? t : t.substring(0, p);
    }

    private int indiceScheda(String con) {
        for (int i = 0; i < schede.getTabCount(); i++) if (nomeScheda(i).equals(con)) return i;
        return -1;
    }

    private JTextPane scheda(String con) {
        JTextPane p = pannelli.get(con);
        if (p == null) {
            p = new JTextPane();
            p.setEditable(false);
            pannelli.put(con, p);
            schede.addTab(con, new JScrollPane(p));
        }
        return p;
    }

    private void modelCambiato(PropertyChangeEvent e) {
        switch (e.getPropertyName()) {
            case "stato", "nome" -> aggiornaStato();
            case "utenti" -> { utenti.clear(); utenti.addAll(model.utenti()); }
            case "conversazione" -> scheda((String) e.getNewValue());
            case "stanze", "stanza" -> {
                aggiornandoStanze = true;
                cmbStanze.removeAllItems();
                for (var v : model.stanze().entrySet()) cmbStanze.addItem(v.getKey() + " (" + v.getValue() + ")");
                if (!model.stanze().containsKey(model.stanza())) cmbStanze.addItem(model.stanza() + " (1)");
                for (int i = 0; i < cmbStanze.getItemCount(); i++)
                    if (cmbStanze.getItemAt(i).startsWith(model.stanza() + " ")) cmbStanze.setSelectedIndex(i);
                aggiornandoStanze = false;
            }
            case "riga" -> aggiungiRiga((ChatModel.Riga) e.getNewValue());
            case "nonLetti" -> {
                String con = (String) e.getNewValue();
                int n = model.nonLetti(con);
                int i = indiceScheda(con);
                if (i >= 0) schede.setTitleAt(i, n == 0 ? con : con + " (" + n + ")");
            }
            case "aperta" -> {
                int i = indiceScheda((String) e.getNewValue());
                if (i >= 0 && schede.getSelectedIndex() != i) schede.setSelectedIndex(i);
            }
            case "svuota" -> {
                while (schede.getTabCount() > 1) schede.removeTabAt(1);
                pannelli.keySet().removeIf(k -> !k.equals(ChatModel.TUTTI));
                pannelli.get(ChatModel.TUTTI).setText("");
                schede.setTitleAt(0, ChatModel.TUTTI);
            }
            case "errore" -> JOptionPane.showMessageDialog(this, e.getNewValue(), "WhatsPAJC", JOptionPane.ERROR_MESSAGE);
            default -> { }
        }
    }

    private void aggiornaStato() {
        boolean connesso = model.stato() == ChatModel.Stato.CONNESSO;
        lblStato.setText(switch (model.stato()) {
            case DISCONNESSO -> "Non connesso";
            case IN_CONNESSIONE -> "Connessione in corso...";
            case CONNESSO -> model.nome() == null ? "Connesso" : "Connesso come " + model.nome();
        });
        btnConnetti.setText(model.stato() == ChatModel.Stato.DISCONNESSO ? "Connetti" : "Disconnetti");
        btnConnetti.setEnabled(model.stato() != ChatModel.Stato.IN_CONNESSIONE);
        txtMessaggio.setEnabled(connesso);
        btnInvia.setEnabled(connesso);
        btnFoto.setEnabled(connesso);
        cmbStanze.setEnabled(connesso);
        if (!connesso) utenti.clear();
    }

    private void aggiungiRiga(ChatModel.Riga r) {
        StyledDocument doc = scheda(r.conversazione()).getStyledDocument();
        try {
            switch (r.tipo()) {
                case PUBBLICO -> {
                    doc.insertString(doc.getLength(), r.da() + ": ", stileNome);
                    doc.insertString(doc.getLength(), r.testo() + "\n", null);
                }
                case PRIVATO -> {
                    doc.insertString(doc.getLength(), r.da() + ": ", stileNome);
                    doc.insertString(doc.getLength(), r.testo() + "\n", stilePrivato);
                }
                case SISTEMA -> doc.insertString(doc.getLength(), r.testo() + "\n", stileSistema);
            }
        } catch (BadLocationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void onInvia(Consumer<String> azione) {
        txtMessaggio.addActionListener(e -> {
            String t = txtMessaggio.getText().strip();
            if (!t.isEmpty()) { azione.accept(t); txtMessaggio.setText(""); }
        });
        btnInvia.addActionListener(e -> {
            String t = txtMessaggio.getText().strip();
            if (!t.isEmpty()) { azione.accept(t); txtMessaggio.setText(""); }
        });
    }

    public void onConnetti(Runnable azione) { btnConnetti.addActionListener(e -> azione.run()); }
    public void onFoto(Runnable azione) { btnFoto.addActionListener(e -> azione.run()); }

    /** L'utente sceglie una stanza nella tendina (non quando e' il Model ad aggiornarla). */
    public void onStanza(Consumer<String> azione) {
        cmbStanze.addActionListener(e -> {
            if (aggiornandoStanze || cmbStanze.getSelectedItem() == null) return;
            String voce = cmbStanze.getSelectedItem().toString();
            azione.accept(voce.substring(0, voce.indexOf(" (")));
        });
    }

    /** Una foto ricevuta: in un dialogo con un'etichetta e un'ImageIcon. */
    public void mostraFoto(String titolo, byte[] dati) {
        javax.swing.ImageIcon icona = new javax.swing.ImageIcon(dati);
        if (icona.getIconWidth() <= 0) { JOptionPane.showMessageDialog(this, "Non e' un'immagine leggibile", titolo, JOptionPane.WARNING_MESSAGE); return; }
        int w = Math.min(icona.getIconWidth(), 700), h = icona.getIconHeight() * w / icona.getIconWidth();
        javax.swing.JLabel l = new javax.swing.JLabel(new javax.swing.ImageIcon(icona.getImage().getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH)));
        javax.swing.JDialog d = new javax.swing.JDialog(this, titolo, false);
        d.add(new JScrollPane(l));
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }
    public void onScheda(Consumer<String> azione) { onScheda = azione; }

    public void onUtenteScelto(Consumer<String> azione) {
        lstUtenti.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && lstUtenti.getSelectedValue() != null) azione.accept(lstUtenti.getSelectedValue());
            }
        });
    }
}
