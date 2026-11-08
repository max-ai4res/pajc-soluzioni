package it.unibs.pajc.redolfi.e03;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Esercizio 5.3, figure con annulla e ripeti.
 *
 * Idea: ogni azione e' un Comando con esegui e annulla (Aggiungi, Sposta con la posizione
 * di prima, Cancella con la figura e il suo indice); Storia tiene le due pile e annuncia
 * "annullabile" e "ripetibile", che il menu Modifica ascolta per abilitare le voci.
 *
 * Errore tipico: un comando Sposta per ogni evento di trascinamento. Qui durante il
 * trascinamento la figura si muove direttamente (anteprima) e il comando nasce una volta,
 * al rilascio, con la posizione di partenza e quella di arrivo.
 */
public class EditorApp {

    private final List<Figura> figure = new ArrayList<>();
    private final Storia storia = new Storia(figure);
    private final JComboBox<Figura.Tipo> tipo = new JComboBox<>(Figura.Tipo.values());

    private JPanel pannello() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (Figura f : figure) {
                    g2.setColor(new Color(0xC8D8F0));
                    g2.fill(f.forma());
                    g2.setColor(new Color(0x24549C));
                    g2.draw(f.forma());
                }
            }
        };
        p.setBackground(Color.WHITE);
        MouseAdapter mouse = new MouseAdapter() {
            Point inizio;
            Figura presa;
            double presaX, presaY;

            @Override
            public void mousePressed(MouseEvent e) {
                inizio = e.getPoint();
                presa = sotto(e.getPoint());
                if (SwingUtilities.isRightMouseButton(e) && presa != null) {
                    storia.esegui(new Cancella(presa));
                    presa = null;
                    p.repaint();
                } else if (presa != null) {
                    presaX = presa.x();
                    presaY = presa.y();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (presa != null) {                     // anteprima: la figura segue il mouse
                    presa.spostaIn(presaX + e.getX() - inizio.x, presaY + e.getY() - inizio.y);
                    p.repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e))
                    return;
                if (presa != null) {
                    if (presa.x() != presaX || presa.y() != presaY)
                        storia.esegui(new Sposta(presa, presaX, presaY, presa.x(), presa.y()));
                } else {
                    double x = Math.min(inizio.x, e.getX()), y = Math.min(inizio.y, e.getY());
                    double w = Math.abs(e.getX() - inizio.x), h = Math.abs(e.getY() - inizio.y);
                    if (w > 3 && h > 3)
                        storia.esegui(new Aggiungi(new Figura((Figura.Tipo) tipo.getSelectedItem(), x, y, w, h)));
                }
                presa = null;
                p.repaint();
            }

            Figura sotto(Point q) {
                for (int i = figure.size() - 1; i >= 0; i--)
                    if (figure.get(i).forma().contains(q))
                        return figure.get(i);
                return null;
            }
        };
        p.addMouseListener(mouse);
        p.addMouseMotionListener(mouse);
        return p;
    }

    private JFrame finestra() {
        JPanel p = pannello();
        JMenuItem annulla = new JMenuItem("Annulla"), ripeti = new JMenuItem("Ripeti");
        annulla.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        ripeti.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        annulla.addActionListener(e -> { storia.annulla(); p.repaint(); });
        ripeti.addActionListener(e -> { storia.ripeti(); p.repaint(); });
        annulla.setEnabled(false);
        ripeti.setEnabled(false);
        storia.addPropertyChangeListener(e -> {         // il menu ascolta le due proprieta'
            if (e.getPropertyName().equals("annullabile"))
                annulla.setEnabled((Boolean) e.getNewValue());
            else
                ripeti.setEnabled((Boolean) e.getNewValue());
        });
        JMenu modifica = new JMenu("Modifica");
        modifica.add(annulla);
        modifica.add(ripeti);
        JMenuBar barra = new JMenuBar();
        barra.add(modifica);
        JFrame f = new JFrame("Figure: trascina per disegnare o spostare, tasto destro cancella");
        f.setJMenuBar(barra);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(tipo, BorderLayout.NORTH);
        f.add(p, BorderLayout.CENTER);
        f.setSize(720, 520);
        return f;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EditorApp().finestra().setVisible(true));
    }
}
