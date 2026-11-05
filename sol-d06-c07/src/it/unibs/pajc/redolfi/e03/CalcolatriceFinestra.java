package it.unibs.pajc.redolfi.e03;

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
 * Palestra 7.3 - il client con la finestra della calcolatrice completa (server:
 * ServerCalcolatriceMT in questo package). STAT compare nello storico come le altre
 * risposte.
 *
 * La connessione resta aperta fra una richiesta e l'altra (classe Connessione, come nel
 * capitolo). Il protocollo e' a passo: ogni richiesta ha esattamente una risposta, nello
 * stesso ordine. Scelta: una CODA delle richieste in attesa (ArrayDeque): ogni risposta
 * che arriva sull'EDT si abbina alla prima richiesta della coda. Cosi' l'utente puo'
 * premere due volte senza aspettare e le righe dello storico restano giuste. Le righe
 * del server passano da una tabella da parola ad azione, speculare a quella delle
 * operazioni sul server: ERR, CIAO e STAT si abbinano alla richiesta in attesa, una
 * riga che inizia con un numero e' un risultato, e INFO (il saluto, le righe di AIUTO)
 * non e' la risposta a passo: va nello storico da sola, e AIUTO non entra nella coda. Lo
 * storico e' una JList con un DefaultListModel di record Voce; gli ERR si colorano in
 * rosso con un renderer. L'apertura della connessione sta in un SwingWorker. Cio' che
 * si scrive parte com'e': la tabella delle operazioni ce l'ha il server, e un ERR torna
 * nello storico.
 *
 * Errore tipico: un solo campo "ultimaRichiesta" sovrascritto dal secondo click prima
 * della prima risposta: la prima risposta viene abbinata alla seconda richiesta.
 */
public class CalcolatriceFinestra extends JFrame {

    private static final long serialVersionUID = 1L;

    /** Una riga dello storico: la richiesta, la sua risposta (null finche' non arriva), se e' un errore. */
    record Voce(String richiesta, String risposta, boolean errore) {}

    private final JTextField txtHost = new JTextField("localhost", 10);
    private final JTextField txtPorta = new JTextField(String.valueOf(Protocollo.PORTA), 5);
    private final JButton btnConnetti = new JButton("Connetti");
    private final JTextField txtEspressione = new JTextField(20);
    private final JButton btnCalcola = new JButton("=");
    private final DefaultListModel<Voce> storico = new DefaultListModel<>();
    private final Deque<Integer> inAttesa = new ArrayDeque<>();   // indici nello storico
    private Connessione connessione;

    /** La prima parola della riga e' la chiave; il resto della riga e' l'argomento. */
    private final Map<String, Consumer<String>> risposte = new HashMap<>();

    public CalcolatriceFinestra() {
        super("Calcolatrice remota");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel nord = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nord.add(new JLabel("Server:"));
        nord.add(txtHost);
        nord.add(txtPorta);
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
                String testo = voce.richiesta() == null ? voce.risposta()
                        : voce.richiesta() + "  ->  " + (voce.risposta() == null ? "..." : voce.risposta());
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
        tabellaRisposte();
        abilita(false);
        setSize(480, 360);
        setLocationRelativeTo(null);
    }

    private void tabellaRisposte() {
        risposte.put("INFO", testo -> storico.addElement(new Voce(null, testo, false)));   // da sola
        risposte.put("ERR", motivo -> risposta("ERR " + motivo, true));
        risposte.put("CIAO", resto -> risposta("ciao", false));
        risposte.put("STAT", resto -> {
            String[] c = Protocollo.argomenti(resto, 2);                   // operazioni e client
            if (c == null) risposta("STAT malformata: " + resto, true);
            else risposta(c[0] + " operazioni, " + c[1] + " client", false);
        });
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
        int porta;
        try {
            porta = Integer.parseInt(txtPorta.getText().strip());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "La porta deve essere un numero", "Calcolatrice remota", JOptionPane.WARNING_MESSAGE);
            return;
        }
        btnConnetti.setEnabled(false);
        new SwingWorker<Connessione, Void>() {
            @Override
            protected Connessione doInBackground() throws IOException {
                return new Connessione(host, porta, CalcolatriceFinestra.this::ricevuta,
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

    /** La riga scritta parte com'e'; se aspetta una risposta a passo, entra nella coda. */
    private void calcola() {
        String testo = txtEspressione.getText().strip();
        if (testo.isEmpty() || connessione == null) return;
        if (!testo.split(" ", 2)[0].equalsIgnoreCase("AIUTO")) {    // AIUTO risponde con INFO, fuori dal passo
            storico.addElement(new Voce(testo, null, false));
            inAttesa.add(storico.size() - 1);                        // la risposta arrivera' per prima
        }
        connessione.invia(testo);
        txtEspressione.selectAll();
    }

    /** Sull'EDT: la tabella speculare decide; una riga che non e' una parola nota e' un risultato. */
    private void ricevuta(String riga) {
        String[] campi = riga.split(" ", 2);
        String resto = campi.length > 1 ? campi[1] : "";
        risposte.getOrDefault(campi[0], r -> risultato(riga)).accept(resto);
    }

    private void risultato(String riga) {
        if (Protocollo.numeri(new String[] {riga.strip()}) == null) risposta("risposta non riconosciuta: " + riga, true);
        else risposta(riga.strip(), false);
    }

    /** La risposta e' per la richiesta piu' vecchia in attesa. */
    private void risposta(String testo, boolean errore) {
        Integer indice = inAttesa.poll();
        if (indice == null) return;                      // una risposta di troppo: ignorata
        storico.set(indice, new Voce(storico.get(indice).richiesta(), testo, errore));
    }

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
