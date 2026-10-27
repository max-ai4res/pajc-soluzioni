package it.unibs.pajc.redolfi.e04;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

/**
 * Problema 9.4 - un visualizzatore di immagini.
 *
 * La lista ha un DefaultListModel di File, riempito a ogni cartella; il renderer mostra il
 * nome e, in HTML (le JLabel lo accettano), la dimensione in grigio. Alla selezione
 * l'immagine si legge una volta con ImageIO e si passa al pannello, che la disegna con un
 * solo fattore di scala, il minore dei due rapporti, e la centra.
 *
 * Errore tipico: leggere il file in paintComponent (a ogni ridisegno), o scalare larghezza
 * e altezza con fattori diversi, deformando l'immagine.
 */
public class Visualizzatore extends JFrame {

    private static final long serialVersionUID = 1L;

    /** Il pannello che disegna un'immagine adattata e centrata. */
    static class PnlImmagine extends JPanel {
        private static final long serialVersionUID = 1L;
        private transient BufferedImage img;

        PnlImmagine() { setBackground(new Color(0x20, 0x24, 0x2A)); }

        void setImmagine(BufferedImage img) { this.img = img; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img == null)
                return;
            double scala = Math.min((double) getWidth() / img.getWidth(), (double) getHeight() / img.getHeight());
            int w = (int) (img.getWidth() * scala), h = (int) (img.getHeight() * scala);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(img, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
            g2.dispose();
        }
    }

    private final DefaultListModel<File> mdlFile = new DefaultListModel<>();
    private final JList<File> lstFile = new JList<>(mdlFile);
    private final PnlImmagine pnlImmagine = new PnlImmagine();
    private File cartella = new File(System.getProperty("user.home"));

    public Visualizzatore() {
        super("Immagini");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JMenu mnu = new JMenu("File");
        JMenuItem apri = new JMenuItem("Apri cartella...");
        apri.addActionListener(e -> scegliCartella());
        mnu.add(apri);
        JMenuBar barra = new JMenuBar();
        barra.add(mnu);
        setJMenuBar(barra);

        lstFile.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstFile.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                File f = (File) value;
                setText("<html>" + f.getName() + " <font color=gray>" + (f.length() + 1023) / 1024 + " kB</font></html>");
                return this;
            }
        });
        lstFile.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                mostra(lstFile.getSelectedValue());
        });

        JScrollPane scroll = new JScrollPane(lstFile);
        scroll.setPreferredSize(new Dimension(240, 0));
        pnlImmagine.setPreferredSize(new Dimension(640, 480));
        add(scroll, BorderLayout.WEST);
        add(pnlImmagine, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    private void scegliCartella() {
        JFileChooser fc = new JFileChooser(cartella);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
        cartella = fc.getSelectedFile();
        File[] immagini = cartella.listFiles((d, n) -> n.toLowerCase(Locale.ROOT).matches(".*\\.(png|jpe?g|gif)"));
        mdlFile.clear();
        if (immagini != null) {
            Arrays.sort(immagini);
            for (File f : immagini)
                mdlFile.addElement(f);
        }
        pnlImmagine.setImmagine(null);
        setTitle("Immagini - " + cartella.getName());
    }

    private void mostra(File f) {
        if (f == null)
            return;
        try {
            BufferedImage img = ImageIO.read(f);
            if (img == null)
                throw new IOException("formato non riconosciuto");
            pnlImmagine.setImmagine(img);
        } catch (IOException e) {
            pnlImmagine.setImmagine(null);
            JOptionPane.showMessageDialog(this, "Impossibile leggere " + f.getName() + ":\n" + e.getMessage(),
                    "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Visualizzatore().setVisible(true));
    }
}
