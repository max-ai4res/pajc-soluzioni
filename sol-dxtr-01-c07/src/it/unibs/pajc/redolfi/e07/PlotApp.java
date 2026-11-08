package it.unibs.pajc.redolfi.e07;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

/**
 * Esercizio 7.7: il Plot completo (chiude il problema d'apertura della dispensa).
 *
 * Tutte le versioni dei capitoli messe insieme:
 * - Catalogo: le funzioni di Math scoperte per reflection, le classi interne e i
 *   metodi annotati @Plot, ciascuna con nome e intervallo (cap. 3 e 4);
 * - Contatore: un proxy che conta le valutazioni per la riga di stato (cap. 4);
 * - Plugin: la cartella plugin/ letta all'avvio e con Ricarica, un loader nuovo ogni
 *   volta, ServiceLoader (cap. 5);
 * - Compilatore + Espressione: il campo dell'espressione compilato in un SwingWorker,
 *   l'errore tradotto sulla colonna e il campo colorato (cap. 6);
 * - SalvaPlugin: "Salva come plugin" scrive il .jar in plugin/ e ricarica (cap. 6);
 * - se il compilatore non c'e' (JRE) la finestra lo dice e il resto funziona;
 * - il menu Aiuto avverte che espressioni e plugin sono codice eseguito con i permessi
 *   dell'utente.
 *
 * Errore tipico: una lista di voci a cui si aggiunge senza svuotare a ogni ricarica
 * (tutti i loader vecchi restano vivi), e la compilazione nel listener.
 */
public class PlotApp {

    private static final Path CARTELLA_PLUGIN = Path.of("plugin");

    private final JFrame frame = new JFrame("Plot di funzioni");
    private final PnlPlot pnlPlot = new PnlPlot();
    private final Catalogo catalogo = new Catalogo();
    private final Contatore contatore = new Contatore();
    private final Plugin plugin = new Plugin(CARTELLA_PLUGIN);
    private final JComboBox<Voce> cmbFunzione = new JComboBox<>();
    private final JTextField txtEspressione = new JTextField("sin(x) * exp(-x / 5)", 26);
    private final JTextField txtNome = new JTextField("la mia", 8);
    private final JTextField txtDa = new JTextField("-10", 4), txtA = new JTextField("10", 4);
    private final JButton btnDisegna = new JButton("Disegna"), btnSalva = new JButton("Salva come plugin");
    private final JLabel lblStato = new JLabel(" ");
    private Compilatore compilatore;
    private Espressione ultima;                       // l'ultima compilata con successo

    public PlotApp() {
        // la barra del menu a tendina
        JPanel barraMenu = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barraMenu.add(new JLabel("f(x) ="));
        barraMenu.add(cmbFunzione);
        JButton btnRicarica = new JButton("Ricarica");
        btnRicarica.addActionListener(e -> ricarica());
        barraMenu.add(btnRicarica);
        cmbFunzione.addActionListener(e -> disegnaVoce());
        // la barra dell'espressione
        JPanel barraEspr = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barraEspr.add(new JLabel("oppure f(x) ="));
        barraEspr.add(txtEspressione);
        barraEspr.add(new JLabel("nome"));
        barraEspr.add(txtNome);
        barraEspr.add(new JLabel("x da"));
        barraEspr.add(txtDa);
        barraEspr.add(new JLabel("a"));
        barraEspr.add(txtA);
        barraEspr.add(btnDisegna);
        barraEspr.add(btnSalva);
        btnDisegna.addActionListener(e -> disegnaEspressione());
        txtEspressione.addActionListener(e -> disegnaEspressione());
        btnSalva.addActionListener(e -> salvaComePlugin());
        btnSalva.setEnabled(false);
        JPanel sopra = new JPanel(new BorderLayout());
        sopra.add(barraMenu, BorderLayout.NORTH);
        sopra.add(barraEspr, BorderLayout.SOUTH);

        frame.setJMenuBar(menu());
        frame.add(sopra, BorderLayout.NORTH);
        frame.add(pnlPlot, BorderLayout.CENTER);
        frame.add(lblStato, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);

        try {
            compilatore = new Compilatore();
        } catch (IllegalStateException | IOException e) {
            btnDisegna.setEnabled(false);
            txtEspressione.setEnabled(false);
            txtEspressione.setToolTipText(e.getMessage());
            lblStato.setText("espressioni non disponibili: " + e.getMessage());
        }
        ricarica();
    }

    private JMenuBar menu() {
        JMenuBar barra = new JMenuBar();
        JMenu aiuto = new JMenu("Aiuto");
        JMenuItem avviso = new JMenuItem("Sicurezza...");
        avviso.addActionListener(e -> JOptionPane.showMessageDialog(frame, """
                Le espressioni scritte nel campo vengono compilate ed eseguite come codice Java
                dentro questo programma, con i tuoi permessi: possono fare tutto cio' che puoi
                fare tu. Lo stesso vale per i .jar nella cartella plugin/: mettici solo codice
                di cui ti fidi, come faresti con un programma qualunque.""",
                "Espressioni e plugin sono codice", JOptionPane.WARNING_MESSAGE));
        aiuto.add(avviso);
        barra.add(aiuto);
        return barra;
    }

    /** Il men\`u: il catalogo interno piu' i plugin, sostituendo tutto (niente voci vecchie vive). */
    private void ricarica() {
        List<Voce> voci = new ArrayList<>(catalogo.voci());
        String nota;
        try {
            Files.createDirectories(CARTELLA_PLUGIN);
            List<Voce> daPlugin = plugin.ricarica();
            voci.addAll(daPlugin);
            nota = daPlugin.size() + " da plugin/";
        } catch (IOException | RuntimeException | LinkageError e) {
            nota = "plugin/ non leggibile: " + e.getMessage();
        }
        Object scelta = cmbFunzione.getSelectedItem();
        cmbFunzione.setModel(new DefaultComboBoxModel<>(voci.toArray(new Voce[0])));
        if (scelta != null)
            for (Voce v : voci)
                if (v.nome().equals(scelta.toString()))
                    cmbFunzione.setSelectedItem(v);
        lblStato.setText(voci.size() + " funzioni nel menu, " + nota);
    }

    private void disegnaVoce() {
        Voce voce = (Voce) cmbFunzione.getSelectedItem();
        if (voce == null)
            return;
        Funzione contata = contatore.conta(voce.f());
        contatore.azzera();
        pnlPlot.setIntervallo(voce.xMin(), voce.xMax());
        pnlPlot.setFunzione(contata::f);
        pnlPlot.paintImmediately(0, 0, pnlPlot.getWidth(), pnlPlot.getHeight());
        lblStato.setText(voce + " su [" + voce.xMin() + ", " + voce.xMax() + "]: "
                + contatore.chiamate() + " valutazioni per il ridisegno");
    }

    private void disegnaEspressione() {
        Double da = leggi(txtDa), a = leggi(txtA);
        if (da == null || a == null || da >= a) {
            lblStato.setText("intervallo non valido");
            return;
        }
        Espressione esp = new Espressione(txtEspressione.getText(), txtNome.getText(), da, a);
        btnDisegna.setEnabled(false);
        lblStato.setText("compilo...");
        new SwingWorker<Funzione, Void>() {
            @Override
            protected Funzione doInBackground() throws ErroreDiCompilazione {
                return esp.compila(compilatore);              // fuori dall'EDT
            }

            @Override
            protected void done() {                           // sull'EDT
                btnDisegna.setEnabled(true);
                try {
                    Funzione f = contatore.conta(get());
                    contatore.azzera();
                    ultima = esp;
                    btnSalva.setEnabled(true);
                    txtEspressione.setBackground(UIManager.getColor("TextField.background"));
                    pnlPlot.setIntervallo(da, a);
                    pnlPlot.setFunzione(f::f);
                    pnlPlot.paintImmediately(0, 0, pnlPlot.getWidth(), pnlPlot.getHeight());
                    lblStato.setText("f(x) = " + txtEspressione.getText() + ": " + contatore.chiamate()
                            + " valutazioni");
                } catch (ExecutionException e) {
                    txtEspressione.setBackground(new Color(0xFFCDD2));
                    if (e.getCause() instanceof ErroreDiCompilazione errore) {
                        lblStato.setText(Espressione.traduci(errore.getDiagnostiche().get(0)));
                        System.out.println(errore.getSorgente());    // nel log, per chi deve capire
                    } else {
                        lblStato.setText("errore: " + e.getCause());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void salvaComePlugin() {
        if (ultima == null)
            return;
        try {
            Files.createDirectories(CARTELLA_PLUGIN);
            Path jar = CARTELLA_PLUGIN.resolve(ultima.nomeBinario().replace('.', '-') + ".jar");
            SalvaPlugin.scrivi(jar, compilatore.ultimaUscita(), List.of(ultima.nomeBinario()));
            ricarica();
            lblStato.setText("salvata in " + jar + ": ora e' nel menu, e si puo' copiare a un collega");
        } catch (IOException e) {
            lblStato.setText("impossibile salvare: " + e.getMessage());
        }
    }

    private static Double leggi(JTextField campo) {
        try {
            return Double.parseDouble(campo.getText().strip().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new PlotApp().frame.setVisible(true));
    }
}
