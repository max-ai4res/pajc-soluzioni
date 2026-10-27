package it.unibs.pajc.redolfi.e09;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/**
 * Esercizio 2.9 - un pannello di controllo che regge caratteri piu' grandi.
 *
 * A ovest la colonna di pulsanti (GridLayout(0, 1): stessa larghezza) sta nel NORTH di un
 * pannello BorderLayout, e il riquadro colorato nel suo SOUTH: i pulsanti restano in alto,
 * il riquadro in fondo, lo spazio fra i due resta vuoto. Al centro il registro in uno
 * scroll pane; a sud la barra di stato e' un BorderLayout con un'etichetta a WEST e una a
 * EAST. Margini: bordo vuoto di 8 sul pannello esterno, 4 di spazio in tutti i layout.
 *
 * ingrandisciFont scorre la tabella dei valori predefiniti di Swing e sostituisce ogni font
 * con uno piu' grande del fattore dato. Va chiamato prima di creare i componenti, che
 * leggono il font dalla tabella quando nascono. Con il 50% in piu' la finestra esce piu'
 * grande e nessun componente e' tagliato, perche' nessuna misura e' scritta a mano (il
 * riquadro ha solo un'altezza preferita, la larghezza la decide il layout).
 *
 * Errore tipico: dare ai pulsanti una dimensione con setPreferredSize, che con i caratteri
 * piu' grandi taglia il testo.
 */
public class PannelloDiControllo extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String[] COMANDI = { "Avvia", "Ferma", "Azzera", "Esporta...", "Opzioni..." };

    public PannelloDiControllo() {
        super("Pannello di controllo");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setJMenuBar(creaMenu());

        // --- ovest: comandi in alto, riquadro in fondo
        JPanel pnlComandi = new JPanel(new GridLayout(0, 1, 4, 4));
        for (String c : COMANDI)
            pnlComandi.add(new JButton(c));

        JPanel pnlCampione = new JPanel();
        pnlCampione.setBackground(new Color(70, 130, 180));
        pnlCampione.setPreferredSize(new Dimension(0, 40));
        pnlCampione.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        JPanel pnlOvest = new JPanel(new BorderLayout(0, 4));
        pnlOvest.add(pnlComandi, BorderLayout.NORTH);
        pnlOvest.add(pnlCampione, BorderLayout.SOUTH);

        // --- centro: il registro
        JTextArea txtRegistro = new JTextArea(16, 48);
        txtRegistro.setEditable(false);
        txtRegistro.setFont(new Font(Font.MONOSPACED, Font.PLAIN, txtRegistro.getFont().getSize()));
        txtRegistro.setText("10:00:00  avvio\n10:00:01  pronto\n");

        // --- sud: stato a sinistra e a destra
        JPanel pnlStato = new JPanel(new BorderLayout(4, 0));
        pnlStato.add(new JLabel("Pronto"), BorderLayout.WEST);
        pnlStato.add(new JLabel("2 righe"), BorderLayout.EAST);

        JPanel pnlTutto = new JPanel(new BorderLayout(4, 4));
        pnlTutto.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pnlTutto.add(pnlOvest, BorderLayout.WEST);
        pnlTutto.add(new JScrollPane(txtRegistro), BorderLayout.CENTER);
        pnlTutto.add(pnlStato, BorderLayout.SOUTH);
        setContentPane(pnlTutto);

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    private JMenuBar creaMenu() {
        JMenu mnuFile = new JMenu("File");
        mnuFile.add(new JMenuItem("Apri..."));
        mnuFile.add(new JMenuItem("Salva"));
        mnuFile.addSeparator();
        mnuFile.add(new JMenuItem("Esci"));

        JMenu mnuAiuto = new JMenu("Aiuto");
        mnuAiuto.add(new JMenuItem("Informazioni"));

        JMenuBar barra = new JMenuBar();
        barra.add(mnuFile);
        barra.add(mnuAiuto);
        return barra;
    }

    /** Moltiplica per fattore tutti i font predefiniti di Swing. */
    static void ingrandisciFont(float fattore) {
        Enumeration<Object> chiavi = UIManager.getDefaults().keys();
        while (chiavi.hasMoreElements()) {
            Object chiave = chiavi.nextElement();
            if (UIManager.get(chiave) instanceof FontUIResource f)
                UIManager.put(chiave, new FontUIResource(f.deriveFont(f.getSize2D() * fattore)));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ingrandisciFont(1.5f);          // togliere per la versione normale
            new PannelloDiControllo().setVisible(true);
        });
    }
}
