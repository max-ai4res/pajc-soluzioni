package it.unibs.pajc.redolfi.e08;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import it.unibs.pajc.redolfi.e08.DialogProprieta.Proprieta;

/**
 * Problema 9.8 - la Lavagna completa.
 *
 * Rispetto al capitolo 8: "Nuovo", "Esporta immagine..." e "Ripeti" nei menu; "Annulla" e
 * "Ripeti" sono abilitati solo quando c'e' qualcosa da annullare o ripetere, e lo dice
 * aggiornaStato(), chiamato a ogni cambiamento del pannello: titolo, conteggio e menu non
 * possono divergere. Il pannello avvisa con un ChangeEvent quando il disegno cambia (e
 * allora il disegno e' modificato) e con le proprieta' "zoom", "puntatore" e "riproduzione"
 * per tutto il resto. L'immagine esportata e' quella dell'area, senza la griglia.
 *
 * Errore tipico: abilitare "Annulla" nel solo listener del mouse, e dimenticare che anche
 * Apri, Nuovo, la riproduzione e lo stesso "Annulla" cambiano lo stato della storia.
 */
public class Lavagna extends JFrame {

    private static final long serialVersionUID = 1L;

    private final SelettoreForma selForma = new SelettoreForma();
    private final DefaultComboBoxModel<Colore> mdlColori = new DefaultComboBoxModel<>(Colore.PALETTE);
    private final JComboBox<Colore> cmbColore = new JComboBox<>(mdlColori);
    private final JButton btnColore = new JButton("Altro...");
    private final JSlider sldSpessore = new JSlider(1, 20, 3);
    private final JComboBox<String> cmbZoom =
            new JComboBox<>(new String[] { "25%", "50%", "100%", "200%", "400%", "800%" });
    private final PnlLavagna pnlLavagna = new PnlLavagna();
    private final JLabel lblStato = new JLabel();

    private Proprieta proprieta = new Proprieta(false, 20, Color.WHITE);
    private File file;                              // null finche' non si salva
    private File cartella = new File(System.getProperty("user.home"));
    private boolean modificato;
    private JMenuItem voceAnnulla, voceRipeti;

    public Lavagna() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);  // la X la gestiamo noi
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                esci();
            }
        });
        setJMenuBar(creaMenu());
        add(creaBarra(), BorderLayout.NORTH);

        pnlLavagna.setPreferredSize(new Dimension(640, 400));
        applicaProprieta();
        add(pnlLavagna, BorderLayout.CENTER);

        lblStato.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(lblStato, BorderLayout.SOUTH);

        cmbColore.setRenderer(new ColoreRenderer());
        selForma.addActionListener(e -> aggiornaStato());
        cmbColore.addActionListener(e -> aggiornaStato());
        pnlLavagna.addPropertyChangeListener("zoom", e ->
                cmbZoom.setSelectedItem(Math.round(pnlLavagna.getZoom() * 100) + "%"));
        pnlLavagna.addPropertyChangeListener("puntatore", e -> aggiornaStato());
        pnlLavagna.addPropertyChangeListener("riproduzione", e -> aggiornaStato());
        pnlLavagna.addChangeListener(e -> {
            segnaModificato();
            aggiornaStato();
        });
        sldSpessore.addChangeListener(e -> aggiornaStato());
        btnColore.addActionListener(e -> scegliColore());
        aggiornaStato();
        aggiornaTitolo();

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    // ---------------------------------------------------------------- barra e menu

    private JPanel creaBarra() {
        JPanel pnlBarra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        pnlBarra.add(selForma);
        pnlBarra.add(cmbColore);
        pnlBarra.add(btnColore);
        pnlBarra.add(new JLabel("Spessore:"));
        pnlBarra.add(sldSpessore);
        pnlBarra.add(new JLabel("Zoom:"));
        pnlBarra.add(cmbZoom);
        cmbZoom.setSelectedItem("100%");
        cmbZoom.addActionListener(e -> {
            String s = (String) cmbZoom.getSelectedItem();
            pnlLavagna.setZoom(Integer.parseInt(s.replace("%", "")) / 100.0);
        });
        return pnlBarra;
    }

    private JMenuBar creaMenu() {
        JMenu mnuFile = new JMenu("File");
        mnuFile.add(voce("Nuovo", this::nuovo));
        mnuFile.add(voce("Apri...", this::apri));
        mnuFile.add(voce("Salva", this::salva));
        mnuFile.add(voce("Salva come...", this::salvaCome));
        mnuFile.addSeparator();
        mnuFile.add(voce("Esporta immagine...", this::esporta));
        mnuFile.add(voce("Proprietà...", this::modificaProprieta));
        mnuFile.addSeparator();
        mnuFile.add(voce("Esci", this::esci));
        int menu = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();   // Ctrl, o Cmd su macOS
        JMenu mnuModifica = new JMenu("Modifica");
        voceAnnulla = voce("Annulla", pnlLavagna::annulla);
        voceAnnulla.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, menu));
        mnuModifica.add(voceAnnulla);
        voceRipeti = voce("Ripeti", pnlLavagna::ripeti);
        voceRipeti.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, menu));
        mnuModifica.add(voceRipeti);
        JMenuItem riproduci = voce("Riproduci", pnlLavagna::riproduci);
        riproduci.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, menu));
        mnuModifica.add(riproduci);

        JMenuBar barra = new JMenuBar();
        barra.add(mnuFile);
        barra.add(mnuModifica);
        return barra;
    }

    private static JMenuItem voce(String testo, Runnable azione) {
        JMenuItem v = new JMenuItem(testo);
        v.addActionListener(e -> azione.run());
        return v;
    }

    private void aggiornaStato() {
        Colore c = (Colore) cmbColore.getSelectedItem();
        pnlLavagna.setForma(selForma.getForma());
        pnlLavagna.setColore(c.colore());
        pnlLavagna.setSpessore(sldSpessore.getValue());
        int n = pnlLavagna.getFigure().size();
        Point2D p = pnlLavagna.getPuntatore();
        String dove = p == null ? "" : String.format("  |  x = %.1f   y = %.1f", p.getX(), p.getY());
        if (voceAnnulla != null) {
            voceAnnulla.setEnabled(pnlLavagna.puoAnnullare());
            voceRipeti.setEnabled(pnlLavagna.puoRipetere());
        }
        lblStato.setText((n == 1 ? "1 figura" : n + " figure") + "  |  "
                + selForma.getForma().etichetta() + ", " + c.nome()
                + ", spessore " + sldSpessore.getValue() + dove);
    }

    private void applicaProprieta() {
        pnlLavagna.setBackground(proprieta.sfondo());
        pnlLavagna.setGriglia(proprieta.griglia(), proprieta.passo());
    }

    private void aggiornaTitolo() {
        String nome = file == null ? "senza nome" : file.getName();
        setTitle("Lavagna - " + nome + (modificato ? " *" : ""));
    }

    private void segnaModificato() {
        modificato = true;
        aggiornaTitolo();
    }

    // ---------------------------------------------------------------- i dialoghi

    private void scegliColore() {
        Colore attuale = (Colore) cmbColore.getSelectedItem();
        Color c = JColorChooser.showDialog(this, "Altro colore", attuale.colore());
        if (c == null)
            return;                                 // annullato: niente cambia
        Colore nuovo = new Colore(String.format("#%02X%02X%02X",
                c.getRed(), c.getGreen(), c.getBlue()), c);
        mdlColori.addElement(nuovo);             // il modello avvisa la tendina...
        mdlColori.setSelectedItem(nuovo);        // ...che si aggiorna da sola
    }

    private void modificaProprieta() {
        new DialogProprieta(this, proprieta).chiedi().ifPresent(p -> {
            proprieta = p;
            applicaProprieta();
            segnaModificato();
        });
    }

    private void nuovo() {
        if (!puoAbbandonare())
            return;
        pnlLavagna.setFigure(List.of());
        file = null;
        modificato = false;
        aggiornaTitolo();
        aggiornaStato();
    }

    private void esporta() {
        JFileChooser fc = new JFileChooser(cartella);
        fc.setFileFilter(new FileNameExtensionFilter("Immagini PNG (*.png)", "png"));
        fc.setSelectedFile(new File(cartella, "disegno.png"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".png"))
            f = new File(f.getPath() + ".png");
        if (f.exists() && !confermaSostituzione(f))
            return;
        try {
            javax.imageio.ImageIO.write(pnlLavagna.immagine(), "png", f);
        } catch (IOException e) {
            errore("Impossibile scrivere " + f.getName(), e);
        }
    }

    private void apri() {
        if (!puoAbbandonare())
            return;
        JFileChooser fc = new JFileChooser(cartella);
        fc.setFileFilter(new FileNameExtensionFilter("Disegni della Lavagna (*.lav)", "lav"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            leggi(fc.getSelectedFile());
    }

    private boolean salva() {
        return file == null ? salvaCome() : scrivi(file);
    }

    private boolean salvaCome() {
        JFileChooser fc = new JFileChooser(cartella);
        fc.setFileFilter(new FileNameExtensionFilter("Disegni della Lavagna (*.lav)", "lav"));
        fc.setSelectedFile(file != null ? file : new File(cartella, "disegno.lav"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return false;
        File f = fc.getSelectedFile();
        if (!f.getName().endsWith(".lav"))
            f = new File(f.getPath() + ".lav");
        if (f.exists() && !confermaSostituzione(f))
            return false;
        return scrivi(f);
    }

    private boolean confermaSostituzione(File f) {
        int r = JOptionPane.showConfirmDialog(this, f.getName() + " esiste già. Sostituirlo?",
                "Salva come", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return r == JOptionPane.YES_OPTION;
    }

    /** true se si puo' andare avanti: niente da salvare, salvato, o modifiche scartate. */
    private boolean puoAbbandonare() {
        if (!modificato)
            return true;
        String[] scelte = { "Salva", "Non salvare", "Annulla" };
        int r = JOptionPane.showOptionDialog(this,
                "Il disegno è cambiato. Salvare le modifiche?", "Lavagna",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, scelte, scelte[0]);
        return switch (r) {
            case 0 -> salva();            // avanti solo se ha salvato davvero
            case 1 -> true;
            default -> false;             // Annulla, o la X del dialogo
        };
    }

    private void esci() {
        if (puoAbbandonare())
            dispose();                    // l'ultima finestra: il programma finisce
    }

    // ---------------------------------------------------------------- il file

    private boolean scrivi(File f) {
        try (PrintWriter out = new PrintWriter(new FileWriter(f))) {
            out.println("LAVAGNA 3");
            out.println(proprieta.griglia() + " " + proprieta.passo() + " " + proprieta.sfondo().getRGB());
            for (Figura fig : pnlLavagna.getFigure())
                out.println(riga(fig));
        } catch (IOException e) {
            errore("Impossibile scrivere " + f.getName(), e);
            return false;
        }
        file = f;
        cartella = f.getParentFile();
        modificato = false;
        aggiornaTitolo();
        return true;
    }

    private void leggi(File f) {
        try (BufferedReader in = new BufferedReader(new FileReader(f))) {
            if (!"LAVAGNA 3".equals(in.readLine()))
                throw new IOException("non è un disegno della Lavagna");
            String[] p = in.readLine().split(" ");
            proprieta = new Proprieta(Boolean.parseBoolean(p[0]), Integer.parseInt(p[1]),
                    new Color(Integer.parseInt(p[2])));
            List<Figura> lette = new ArrayList<>();
            String riga;
            while ((riga = in.readLine()) != null)
                lette.add(figura(riga));
            pnlLavagna.setFigure(lette);
        } catch (IOException | RuntimeException e) {
            errore("Impossibile leggere " + f.getName(), e);
            return;
        }
        applicaProprieta();
        aggiornaStato();
        file = f;
        cartella = f.getParentFile();
        modificato = false;
        aggiornaTitolo();
    }

    /** Una figura in una riga: tipo, colore, spessore, punti. "RETTANGOLO -16777216 3.0 10,20 80,60" */
    private static String riga(Figura f) {
        StringBuilder sb = new StringBuilder(f.tipo().name() + " " + f.colore().getRGB()
                + " " + f.spessore());
        for (Point2D p : f.punti())
            sb.append(' ').append(p.getX()).append(',').append(p.getY());
        return sb.toString();
    }

    private static Figura figura(String riga) {
        String[] parti = riga.split(" ");
        List<Point2D> punti = new ArrayList<>();
        for (int i = 3; i < parti.length; i++) {
            String[] xy = parti[i].split(",");
            punti.add(new Point2D.Double(Double.parseDouble(xy[0]), Double.parseDouble(xy[1])));
        }
        return new Figura(Forma.valueOf(parti[0]), new Color(Integer.parseInt(parti[1])),
                Float.parseFloat(parti[2]), punti);
    }

    private void errore(String messaggio, Exception e) {
        JOptionPane.showMessageDialog(this, messaggio + ":\n" + e.getMessage(),
                "Errore", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Lavagna().setVisible(true));
    }
}
