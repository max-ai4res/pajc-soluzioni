package it.unibs.pajc.redolfi.e07;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Esercizio 5.7 - la calcolatrice remota in finestra (server: sol-d06-c03, e06).
 *
 * La connessione resta aperta fra una richiesta e l'altra (classe Connessione, come nel
 * capitolo) e consegna righe. Il protocollo e' a passo: ogni richiesta ha esattamente
 * una risposta, nello stesso ordine. Scelta: una CODA delle richieste in attesa
 * (ArrayDeque): ogni riga che arriva sull'EDT si abbina alla prima richiesta della
 * coda. Cosi' l'utente puo' premere due volte senza aspettare e le righe dello storico
 * restano giuste. Le righe del server si interpretano con una tabella da parola ad
 * azione (ERR e CIAO; tutto il resto e' un numero, il risultato), speculare alla
 * tabella dei comandi del server; prima di mandare, la riga dell'utente passa da
 * Protocollo.controlla, cosi' una riga sbagliata finisce nello storico senza disturbare
 * il server. Lo storico e' una JList con un DefaultListModel di record Voce; gli ERR si
 * colorano in rosso con un renderer. L'apertura della connessione sta in un SwingWorker.
 *
 * Errore tipico: un solo campo "ultimaRichiesta" sovrascritto dal secondo click prima
 * della prima risposta: la prima risposta viene abbinata alla seconda richiesta.
 */
public class CalcolatriceFinestra extends JFrame {

    private static final long serialVersionUID = 1L;

    /** Una riga dello storico: la richiesta, la risposta (null finche' non arriva), se e' un ERR. */
    record Voce(String richiesta, String risposta, boolean errore) {}

    private final JTextField txtHost = new JTextField("localhost", 10);
    private final JButton btnConnetti = new JButton("Connetti");
    private final JTextField txtEspressione = new JTextField(20);
    private final JButton btnCalcola = new JButton("=");
    private final DefaultListModel<Voce> storico = new DefaultListModel<>();
    private final Deque<Integer> inAttesa = new ArrayDeque<>();   // indici nello storico
    private Connessione connessione;

    /** La prima parola della riga del server e' la chiave; il resto e' l'argomento. */
    private final Map<String, Consumer<String>> risposte = new HashMap<>();

    public CalcolatriceFinestra() {
        super("Calcolatrice remota");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel nord = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nord.add(new JLabel("Server:"));
        nord.add(txtHost);
        nord.add(btnConnetti);
        JPanel sud = new JPanel(new BorderLayout(6, 0));
        sud.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        sud.add(txtEspressione, BorderLayout.CENTER);
        sud.add(btnCalcola, BorderLayout.EAST);
        JList<Voce> lst = new JList<>(storico);
        lst.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foc) {
                Voce voce = (Voce) v;
                String testo = voce.richiesta() + "  ->  " + (voce.risposta() == null ? "..." : voce.risposta());
                super.getListCellRendererComponent(l, testo, i, sel, foc);
                setForeground(voce.errore() ? Color.RED : l.getForeground());
                return this;
            }
        });
        add(nord, BorderLayout.NORTH);
        add(new JScrollPane(lst), BorderLayout.CENTER);
        add(sud, BorderLayout.SOUTH);
        btnConnetti.addActionListener(e -> connettiODisconnetti());
        btnCalcola.addActionListener(e -> calcola());
        txtEspressione.addActionListener(e -> calcola());
        abilita(false);
        tabellaRisposte();
        setSize(480, 360);
        setLocationRelativeTo(null);
    }

    /** Le righe del server: ERR e CIAO hanno la loro parola; un numero no, ed e' il caso di default. */
    private void tabellaRisposte() {
        risposte.put("ERR", motivo -> abbina("ERR " + motivo, true));
        risposte.put("CIAO", resto -> abbina("ciao", false));
    }

    private void abilita(boolean connesso) {
        txtEspressione.setEnabled(connesso);
        btnCalcola.setEnabled(connesso);
        btnConnetti.setText(connesso ? "Disconnetti" : "Connetti");
    }

    private void connettiODisconnetti() {
        if (connessione != null) {
            connessione.close();
            connessione = null;
            abilita(false);
            return;
        }
        String host = txtHost.getText().strip();
        btnConnetti.setEnabled(false);
        new SwingWorker<Connessione, Void>() {
            @Override
            protected Connessione doInBackground() throws IOException {
                return new Connessione(host, Protocollo.PORTA, CalcolatriceFinestra.this::ricevuta,
                        CalcolatriceFinestra.this::chiusa);
            }
            @Override
            protected void done() {
                btnConnetti.setEnabled(true);
                try {
                    connessione = get();
                    abilita(true);
                    txtEspressione.requestFocusInWindow();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CalcolatriceFinestra.this,
                            "Impossibile connettersi: " + e.getCause().getMessage(),
                            "Calcolatrice remota", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void calcola() {
        String testo = txtEspressione.getText().strip();
        if (testo.isEmpty() || connessione == null) return;
        String motivo = Protocollo.controlla(testo);
        if (motivo != null) {                            // non disturbo il server
            storico.addElement(new Voce(testo, "ERR " + motivo, true));
            return;
        }
        storico.addElement(new Voce(testo, null, false));
        inAttesa.add(storico.size() - 1);                // la risposta arrivera' per prima
        connessione.invia(testo);
        txtEspressione.selectAll();
    }

    /** Sull'EDT: la tabella decide; una parola che non c'e' nella tabella e' un numero. */
    void ricevuta(String riga) {
        String[] campi = riga.split(" ", 2);
        String resto = campi.length > 1 ? campi[1] : "";
        risposte.getOrDefault(campi[0], r -> risultato(riga)).accept(resto);
    }

    private void risultato(String riga) {
        try {
            Long.parseLong(riga.strip());
            abbina(riga.strip(), false);
        } catch (NumberFormatException e) {
            abbina("ERR risposta non riconosciuta: " + riga, true);
        }
    }

    /** La risposta e' per la richiesta piu' vecchia in attesa. */
    private void abbina(String risposta, boolean errore) {
        Integer indice = inAttesa.poll();
        if (indice == null) return;                      // una risposta di troppo: ignorata
        storico.set(indice, new Voce(storico.get(indice).richiesta(), risposta, errore));
    }

    /** Per le prove senza rete: una richiesta messa in attesa come se fosse partita. */
    void inAttesa(String richiesta) {
        storico.addElement(new Voce(richiesta, null, false));
        inAttesa.add(storico.size() - 1);
    }

    DefaultListModel<Voce> storico() { return storico; }

    private void chiusa(String motivo) {
        if (connessione == null) return;
        connessione = null;
        abilita(false);
        JOptionPane.showMessageDialog(this, motivo == null ? "Il server ha chiuso" : "Connessione persa: " + motivo,
                "Calcolatrice remota", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CalcolatriceFinestra().setVisible(true));
    }
}
