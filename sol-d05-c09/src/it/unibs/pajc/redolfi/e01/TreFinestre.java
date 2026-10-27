package it.unibs.pajc.redolfi.e01;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Problema 9.1 - le tre finestre da disegnare, per controllare la previsione.
 *
 * (a) Content pane con BorderLayout: "Uno" e "Tre" vanno entrambi in CENTER, e "Tre"
 *     sostituisce "Uno"; "Due" occupa la fascia in alto. Allargando cresce "Tre".
 * (b) Il pannello ha un FlowLayout: "Uno" e "Due" affiancati, centrati, in fondo; l'area di
 *     testo, in CENTER, prende il resto e cresce allargando.
 * (c) GridLayout(0, 2) con cinque pulsanti: 3 righe e 2 colonne, l'ultima cella vuota; a
 *     WEST la griglia e' larga quanto le serve e alta quanto la finestra (i pulsanti si
 *     allungano in altezza); al centro l'etichetta.
 */
public class TreFinestre {

    static JFrame finestra(String titolo, int x) {
        JFrame f = new JFrame(titolo);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // tre finestre: l'ultima chiude
        f.setLocation(x, 100);
        return f;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame a = finestra("(a)", 60);
            a.add(new JButton("Uno"));
            a.add(new JButton("Due"), BorderLayout.NORTH);
            a.add(new JButton("Tre"));
            a.pack();
            a.setVisible(true);

            JFrame b = finestra("(b)", 320);
            JPanel p = new JPanel();
            p.add(new JButton("Uno"));
            p.add(new JButton("Due"));
            b.add(p, BorderLayout.SOUTH);
            b.add(new JTextArea(5, 20));
            b.pack();
            b.setVisible(true);

            JFrame c = finestra("(c)", 640);
            JPanel g = new JPanel(new GridLayout(0, 2));
            for (int i = 1; i <= 5; i++)
                g.add(new JButton("B" + i));
            c.add(g, BorderLayout.WEST);
            c.add(new JLabel("centro", SwingConstants.CENTER));
            c.pack();
            c.setVisible(true);
        });
    }
}
