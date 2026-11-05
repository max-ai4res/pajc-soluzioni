package it.unibs.pajc.redolfi.e09;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.HashMap;
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
 * Esercizio 5.9 - il tris in finestra: client del server arbitro (sol-d06-c04, e09).
 *
 * Model (TrisModel), pannello che si disegna dal Model (PnlGriglia) e questa finestra,
 * che fa anche da Controller: traduce i click in righe /m e le righe del server in
 * modifiche del Model con una tabella da parola ad azione (PARTITA, TOCCA, MOSSA, VINCE,
 * PATTA, ABBANDONO, ERR...), speculare alla tabella dei comandi del server; una riga con
 * i campi che non tornano e' un errore del Model, non un'eccezione. "Non e' il tuo turno"
 * e' uno STATO del Model: il pannello ignora i click quando il turno non e' nostro, senza
 * chiedere al server; l'ERR del server resta per i casi in cui Model e server non sono
 * d'accordo (una casella gia' occupata che il Model non ha ancora visto). La fine della
 * partita apre un dialogo con la proposta di una nuova partita: il server rimette i
 * giocatori in coda da solo, quindi "nuova partita" vuol dire solo aspettare
 * l'accoppiamento; "no" disconnette.
 *
 * Errore tipico: aspettare l'ERR "non e' il tuo turno" per bloccare l'utente: la
 * finestra manda mosse inutili e l'utente vede errori per cose che il client sapeva.
 */
public class TrisFinestra extends JFrame {

    private static final long serialVersionUID = 1L;

    private final TrisModel model;
    private final JTextField txtHost = new JTextField("localhost", 10);
    private final JTextField txtNome = new JTextField("", 8);
    private final JButton btnConnetti = new JButton("Connetti");
    private final JLabel lblStato = new JLabel("Non connesso");
    private Connessione connessione;

    /** La prima parola della riga del server e' la chiave; il resto della riga e' l'argomento. */
    private final Map<String, Consumer<String>> risposte = new HashMap<>();

    public TrisFinestra() {
        this(new TrisModel());
    }

    public TrisFinestra(TrisModel model) {
        super("Tris in rete");
        this.model = model;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel nord = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nord.add(new JLabel("Server:"));
        nord.add(txtHost);
        nord.add(new JLabel("Nome:"));
        nord.add(txtNome);
        nord.add(btnConnetti);
        add(nord, BorderLayout.NORTH);
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
        risposte.put("BENVENUTO", nome -> { });
        risposte.put("PARTITA", resto -> campi(resto, 2, c -> model.nuovaPartita(c[0].charAt(0), c[1])));
        risposte.put("TOCCA", resto -> model.setTurnoMio(true));
        risposte.put("MOSSA", resto -> campi(resto, 3,
                c -> model.mossa(c[0].charAt(0), Integer.parseInt(c[1]), Integer.parseInt(c[2]))));
        risposte.put("VINCE", simbolo -> model.fine(
                simbolo.strip().equals(String.valueOf(model.mio())) ? "Hai vinto!" : "Ha vinto " + model.avversario()));
        risposte.put("PATTA", resto -> model.fine("Patta"));
        risposte.put("ABBANDONO", nome -> model.fine(nome + " ha abbandonato: hai vinto"));
        risposte.put("ERR", model::errore);
        risposte.put("CIAO", resto -> { });
    }

    private void aggiornaStato() {
        lblStato.setText(" " + switch (model.fase()) {
            case DISCONNESSO -> "Non connesso";
            case IN_ATTESA -> "In attesa di un avversario...";
            case IN_CORSO -> (model.turnoMio() ? "Tocca a te (" : "Tocca a " + model.avversario() + " (")
                    + "sei " + model.mio() + ")";
            case FINITA -> model.esito();
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
        btnConnetti.setEnabled(false);
        new SwingWorker<Connessione, Void>() {
            @Override
            protected Connessione doInBackground() throws IOException {
                return new Connessione(host, Protocollo.PORTA, TrisFinestra.this::ricevuta, TrisFinestra.this::chiusa);
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

    /** Sull'EDT: la tabella decide; una parola sconosciuta e' un errore, non un'eccezione. */
    void ricevuta(String riga) {
        String[] campi = riga.split(" ", 2);
        String resto = campi.length > 1 ? campi[1] : "";
        risposte.getOrDefault(campi[0], r -> model.errore("riga del server non riconosciuta: " + riga)).accept(resto);
    }

    /** Spezza il resto in n campi e li passa all'azione; se non tornano (o non sono numeri) e' un errore. */
    private void campi(String resto, int n, Consumer<String[]> azione) {
        String[] c = Protocollo.argomenti(resto, n);
        try {
            if (c == null) throw new IllegalArgumentException();
            azione.accept(c);
        } catch (RuntimeException e) {
            model.errore("riga del server malformata: " + resto);
        }
    }

    private void fine(String esito) {
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
