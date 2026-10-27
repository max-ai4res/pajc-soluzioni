package it.unibs.pajc.redolfi.e09;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Il mini editor dell'esercizio 4.8, copiato qui perche' il package si legga da solo;
 * EditorConTrova lo estende con la finestra "Trova".
 *
 * Lo schema e' quello della Lavagna (sezione 4.6): un campo file (null finche' non si
 * salva), un campo modificato, il titolo che li mostra; puoAbbandonare() pone la domanda
 * "Salva / Non salvare / Annulla" per Nuovo, Apri ed Esci, e la X passa da esci() grazie a
 * DO_NOTHING_ON_CLOSE e a un WindowAdapter. Le modifiche le rileva un DocumentListener sul
 * documento dell'area di testo; mentre il programma carica un file la bandiera caricando
 * evita che il setText conti come una modifica dell'utente.
 *
 * Errore tipico: "Salva" nella domanda di chiusura che restituisce true anche quando
 * l'utente annulla il JFileChooser, e chiude perdendo il testo.
 */
public class MiniEditor extends JFrame {

    private static final long serialVersionUID = 1L;

    protected final JTextArea txtTesto = new JTextArea(25, 70);
    private File file;
    private File cartella = new File(System.getProperty("user.home"));
    private boolean modificato;
    private boolean caricando;

    public MiniEditor() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                esci();
            }
        });

        txtTesto.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        txtTesto.setLineWrap(true);
        txtTesto.setWrapStyleWord(true);
        txtTesto.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { cambiato(); }
            public void removeUpdate(DocumentEvent e) { cambiato(); }
            public void changedUpdate(DocumentEvent e) { cambiato(); }
        });
        add(new JScrollPane(txtTesto), BorderLayout.CENTER);
        setJMenuBar(creaMenu());

        aggiornaTitolo();
        pack();
        setLocationRelativeTo(null);
    }

    protected JMenuBar creaMenu() {
        JMenu mnuFile = new JMenu("File");
        mnuFile.add(voce("Nuovo", this::nuovo));
        mnuFile.add(voce("Apri...", this::apri));
        mnuFile.add(voce("Salva", this::salva));
        mnuFile.add(voce("Salva come...", this::salvaCome));
        mnuFile.addSeparator();
        mnuFile.add(voce("Esci", this::esci));
        JMenuBar barra = new JMenuBar();
        barra.add(mnuFile);
        return barra;
    }

    protected static JMenuItem voce(String testo, Runnable azione) {
        JMenuItem v = new JMenuItem(testo);
        v.addActionListener(e -> azione.run());
        return v;
    }

    private void cambiato() {
        if (caricando || modificato)
            return;
        modificato = true;
        aggiornaTitolo();
    }

    private void aggiornaTitolo() {
        setTitle((file == null ? "senza nome" : file.getName()) + (modificato ? " *" : "")
                + " - Mini editor");
    }

    private void nuovo() {
        if (puoAbbandonare())
            carica(null, "");
    }

    private void apri() {
        if (!puoAbbandonare())
            return;
        JFileChooser fc = new JFileChooser(cartella);
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
        File f = fc.getSelectedFile();
        try {
            carica(f, Files.readString(f.toPath()));
        } catch (IOException e) {
            errore("Impossibile leggere " + f.getName(), e);
        }
    }

    /** Mette nell'editor un testo appena letto (o vuoto), senza segnarlo come modificato. */
    private void carica(File f, String testo) {
        caricando = true;
        try {
            txtTesto.setText(testo);
            txtTesto.setCaretPosition(0);
        } finally {
            caricando = false;
        }
        file = f;
        if (f != null)
            cartella = f.getParentFile();
        modificato = false;
        aggiornaTitolo();
    }

    private boolean salva() {
        return file == null ? salvaCome() : scrivi(file);
    }

    private boolean salvaCome() {
        JFileChooser fc = new JFileChooser(cartella);
        fc.setSelectedFile(file != null ? file : new File(cartella, "senza nome.txt"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return false;
        File f = fc.getSelectedFile();
        if (f.exists() && JOptionPane.showConfirmDialog(this,
                f.getName() + " esiste già. Sostituirlo?", "Salva come",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
            return false;
        return scrivi(f);
    }

    private boolean scrivi(File f) {
        try {
            Files.writeString(f.toPath(), txtTesto.getText());
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

    private boolean puoAbbandonare() {
        if (!modificato)
            return true;
        String[] scelte = { "Salva", "Non salvare", "Annulla" };
        int r = JOptionPane.showOptionDialog(this, "Il testo è cambiato. Salvare le modifiche?",
                "Mini editor", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
                scelte, scelte[0]);
        return switch (r) {
            case 0 -> salva();
            case 1 -> true;
            default -> false;
        };
    }

    private void esci() {
        if (puoAbbandonare())
            dispose();
    }

    private void errore(String messaggio, Exception e) {
        JOptionPane.showMessageDialog(this, messaggio + ":\n" + e.getMessage(), "Errore",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MiniEditor().setVisible(true));
    }
}
