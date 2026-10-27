package it.unibs.pajc.redolfi.e09;

import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Esercizio 4.9 - il mini editor con la finestra "Trova" non modale.
 *
 * La finestra si crea alla prima richiesta e si tiene in un campo: riaprendola compare la
 * stessa, con l'ultimo testo cercato. La voce di menu ha la scorciatoia Ctrl+F (Cmd+F su
 * macOS, grazie a getMenuShortcutKeyMaskEx).
 */
public class EditorConTrova extends MiniEditor {

    private static final long serialVersionUID = 1L;

    private DialogTrova dlgTrova;

    @Override
    protected JMenuBar creaMenu() {
        JMenuBar barra = super.creaMenu();
        JMenu mnuModifica = new JMenu("Modifica");
        JMenuItem trova = voce("Trova...", this::trova);
        int menu = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        trova.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, menu));
        mnuModifica.add(trova);
        barra.add(mnuModifica);
        return barra;
    }

    private void trova() {
        if (dlgTrova == null)
            dlgTrova = new DialogTrova(this, txtTesto);
        dlgTrova.mostra();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EditorConTrova().setVisible(true));
    }
}
