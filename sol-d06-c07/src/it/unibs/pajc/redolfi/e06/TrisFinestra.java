package it.unibs.pajc.redolfi.e06;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Palestra 7.6 - il tris in finestra con gli spettatori: "Guarda..." chiede l'elenco
 * (PARTITE), un dialogo sceglie la partita, STATO porta la griglia corrente e poi le
 * mosse arrivano come ai giocatori; il pannello e' in sola lettura (turnoMio resta false).
 *
 * Model (TrisModel), pannello che si disegna dal Model (PnlGriglia) e questa finestra,
 * che fa anche da Controller: traduce i click in /m e le righe del server in modifiche
 * del Model con una tabella da parola ad azione, speculare a quella dei comandi sul
 * server. "Non e' il tuo turno" e' uno STATO del Model: il pannello ignora i click
 * quando il turno non e' nostro, senza chiedere al server; l'ERR del server resta per i
 * casi in cui Model e server non sono d'accordo (una casella gia' occupata che il Model
 * non ha ancora visto). La fine della partita apre un dialogo con la proposta di una
 * nuova partita: il server rimette i giocatori in coda da solo, quindi "nuova partita"
 * vuol dire solo aspettare l'accoppiamento; "no" disconnette.
 *
 * Errore tipico: aspettare l'ERR "non e' il tuo turno" per bloccare l'utente: la
 * finestra manda mosse inutili e l'utente vede errori per cose che il client sapeva.
 */
public class TrisFinestra extends JFrame {

    private static final long serialVersionUID = 1L;

    private final TrisModel model = new TrisModel();
    private final JTextField txtHost = new JTextField("localhost", 10);
    private final JTextField txtPorta = new JTextField(String.valueOf(Protocollo.PORTA), 5);
    private final JTextField txtNome = new JTextField("", 8);
    private final JButton btnConnetti = new JButton("Connetti");
    private final JButton btnGuarda = new JButton("Guarda...");
    private final JLabel lblStato = new JLabel("Non connesso");
    private Connessione connessione;

    /** La prima parola della riga e' la chiave; il resto della riga e' l'argomento. */
    private final Map<String, Consumer<String>> risposte = new HashMap<>();

    public TrisFinestra() {
        super("Tris in rete");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel nord = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nord.add(new JLabel("Server:"));
        nord.add(txtHost);
        nord.add(txtPorta);
        nord.add(new JLabel("Nome:"));
        nord.add(txtNome);
        nord.add(btnConnetti);
        nord.add(btnGuarda);
        add(nord, BorderLayout.NORTH);
        btnGuarda.addActionListener(e -> { if (connessione != null) connessione.invia("/guarda"); });
        add(new PnlGriglia(model, this::click), BorderLayout.CENTER);
        add(lblStato, BorderLayout.SOUTH);
        model.addPropertyChangeListener(e -> {
            switch (e.getPropertyName()) {
                case "fase", "turno", "griglia" -> aggiornaStato();
                case "esito" -> fine((String) e.getNewValue());
                case "errore" -> JOptionPane.showMessageDialog(this, e.getNewValue(), "Tris", JOptionPane.ERROR_MESSAGE);
                default -> { }
            }
        });
        btnConnetti.addActionListener(e -> connettiODisconnetti());
        tabellaRisposte();
        pack();
        setLocationRelativeTo(null);
    }

    private void tabellaRisposte() {
        risposte.put("INFO", resto -> { });
        risposte.put("BENVENUTO", resto -> { });
        risposte.put("CIAO", resto -> { });
        risposte.put("PARTITA", resto -> {
            String[] a = Protocollo.argomenti(resto, 2);                   // simbolo e avversario
            if (a == null) return;
            model.nuovaPartita(model.fase() == TrisModel.Fase.SPETTATORE ? ' ' : a[0].charAt(0), a[1]);
        });
        risposte.put("PARTITE", elenco -> {
            if (elenco.isEmpty()) { JOptionPane.showMessageDialog(this, "Nessuna partita in corso", "Tris", JOptionPane.INFORMATION_MESSAGE); return; }
            List<String> nomi = List.of(elenco.split(","));
            Object scelta = JOptionPane.showInputDialog(this, "Quale partita?", "Guarda", JOptionPane.QUESTION_MESSAGE, null, nomi.toArray(), nomi.get(0));
            if (scelta != null) connessione.invia("/guarda " + scelta);
        });
        risposte.put("STATO", resto -> {
            String[] a = Protocollo.argomenti(resto, 2);                   // griglia e turno
            if (a != null && a[0].length() == 9) model.stato(a[0], a[1].charAt(0));
        });
        risposte.put("TOCCA", resto -> model.setTurnoMio(true));
        risposte.put("MOSSA", resto -> {
            String[] a = Protocollo.argomenti(resto, 3);                   // simbolo, riga, colonna
            int r = a == null ? -1 : Protocollo.coordinata(a[1]), k = a == null ? -1 : Protocollo.coordinata(a[2]);
            if (r >= 0 && k >= 0) model.mossa(a[0].charAt(0), r, k);
        });
        risposte.put("VINCE", simbolo -> model.fine(model.mio() == ' ' ? "Ha vinto " + simbolo
                : simbolo.charAt(0) == model.mio() ? "Hai vinto!" : "Ha vinto " + model.avversario()));
        risposte.put("PATTA", resto -> model.fine("Patta"));
        risposte.put("ABBANDONO", nome -> model.fine(nome + " ha abbandonato: hai vinto"));
        risposte.put("ERR", model::errore);
    }

    private void aggiornaStato() {
        lblStato.setText(" " + switch (model.fase()) {
            case DISCONNESSO -> "Non connesso";
            case IN_ATTESA -> "In attesa di un avversario...";
            case IN_CORSO -> (model.turnoMio() ? "Tocca a te (" : "Tocca a " + model.avversario() + " (")
                    + "sei " + model.mio() + ")";
            case FINITA -> model.esito();
            case SPETTATORE -> "Guardi " + model.avversario() + " (tocca a " + (model.turnoMio() ? "X" : "O") + ")";
        });
        btnConnetti.setText(model.fase() == TrisModel.Fase.DISCONNESSO ? "Connetti" : "Disconnetti");
    }

    private void click(int r, int c) {
        if (connessione != null && model.cella(r, c) == ' ') connessione.invia("/m " + r + " " + c);
    }

    private void connettiODisconnetti() {
        if (connessione != null) { disconnetti(); return; }
        String host = txtHost.getText().strip(), nome = txtNome.getText().strip();
        if (!Protocollo.nomeValido(nome)) {
            JOptionPane.showMessageDialog(this, "Scegli un nome (2-16 lettere o cifre)", "Tris", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int porta;
        try {
            porta = Integer.parseInt(txtPorta.getText().strip());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "La porta deve essere un numero", "Tris", JOptionPane.WARNING_MESSAGE);
            return;
        }
        btnConnetti.setEnabled(false);
        new SwingWorker<Connessione, Void>() {
            @Override
            protected Connessione doInBackground() throws IOException {
                return new Connessione(host, porta, TrisFinestra.this::ricevuta, TrisFinestra.this::chiusa);
            }
            @Override
            protected void done() {
                btnConnetti.setEnabled(true);
                try {
                    connessione = get();
                    connessione.invia("/nome " + nome);
                    model.setFase(TrisModel.Fase.IN_ATTESA);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(TrisFinestra.this, "Impossibile connettersi: " + e.getCause().getMessage(),
                            "Tris", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void disconnetti() {
        if (connessione != null) { connessione.invia("/esci"); connessione.close(); connessione = null; }
        model.setFase(TrisModel.Fase.DISCONNESSO);
    }

    /** Sull'EDT: la tabella speculare decide; una parola sconosciuta finisce in un dialogo. */
    private void ricevuta(String riga) {
        String[] campi = riga.split(" ", 2);
        String resto = campi.length > 1 ? campi[1] : "";
        risposte.getOrDefault(campi[0], r -> model.errore("riga del server non riconosciuta: " + riga)).accept(resto);
    }

    private void fine(String esito) {
        if (model.mio() == ' ') {                                   // spettatore: torno all'elenco
            JOptionPane.showMessageDialog(this, esito, "Tris", JOptionPane.INFORMATION_MESSAGE);
            if (connessione != null) connessione.invia("/guarda");
            return;
        }
        int scelta = JOptionPane.showConfirmDialog(this, esito + "\nUn'altra partita?", "Tris",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (scelta == JOptionPane.YES_OPTION) model.setFase(TrisModel.Fase.IN_ATTESA);   // il server ci rimette in coda
        else disconnetti();
    }

    private void chiusa(String motivo) {
        if (connessione == null) return;
        connessione = null;
        model.setFase(TrisModel.Fase.DISCONNESSO);
        JOptionPane.showMessageDialog(this, motivo == null ? "Il server ha chiuso" : "Connessione persa: " + motivo,
                "Tris", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TrisFinestra().setVisible(true));
    }
}
