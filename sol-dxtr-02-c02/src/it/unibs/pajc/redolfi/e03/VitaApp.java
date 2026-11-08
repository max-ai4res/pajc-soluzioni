package it.unibs.pajc.redolfi.e03;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Esercizio 2.3, il gioco della vita.
 *
 * Idea: Griglia e' il modello (toro, regole, generazione in una griglia nuova); PnlGriglia
 * disegna e traduce il pixel in cella; il controllore inverte la cella sotto il click e fa
 * scattare una generazione ogni 0,2 s di tempo misurato, accumulando i dt dei passi del
 * timer. ProvaGriglia verifica l'aliante senza finestra.
 *
 * Errore tipico: aggiornare la griglia sul posto, cella per cella: le celle gia' cambiate
 * falsano il conteggio delle vicine e l'aliante si disfa.
 */
public class VitaApp {

    private static final double PERIODO = 0.2;         // secondi fra due generazioni

    private final Griglia griglia = new Griglia(30, 40);
    private final PnlGriglia vista = new PnlGriglia(griglia);
    private final Timer timer = new Timer(16, e -> stepNext());
    private long ultimo;
    private double accumulato;                           // secondi dall'ultima generazione

    public VitaApp() {
        vista.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point cella = vista.cellaDaPixel(e.getPoint());
                if (cella != null) {
                    griglia.inverti(cella.x, cella.y);
                    vista.repaint();
                }
            }
        });
        aliante(2, 2);
    }

    private void aliante(int r, int c) {
        griglia.accendi(r, c + 1);
        griglia.accendi(r + 1, c + 2);
        griglia.accendi(r + 2, c);
        griglia.accendi(r + 2, c + 1);
        griglia.accendi(r + 2, c + 2);
    }

    private void stepNext() {
        long adesso = System.nanoTime();
        accumulato += (adesso - ultimo) / 1e9;
        ultimo = adesso;
        while (accumulato >= PERIODO) {                  // anche piu' di una, dopo un ritardo
            griglia.generazione();
            accumulato -= PERIODO;
        }
        vista.repaint();
    }

    private void avviaFerma() {
        if (timer.isRunning()) {
            timer.stop();
        } else {
            ultimo = System.nanoTime();
            accumulato = 0;
            timer.start();
        }
    }

    private JFrame finestra() {
        JButton avvia = new JButton("Avvia / Ferma");
        avvia.addActionListener(e -> avviaFerma());
        JButton passo = new JButton("Un passo");
        passo.addActionListener(e -> {
            griglia.generazione();
            vista.repaint();
        });
        JButton svuota = new JButton("Svuota");
        svuota.addActionListener(e -> {
            griglia.svuota();
            vista.repaint();
        });
        JPanel comandi = new JPanel(new FlowLayout());
        comandi.add(avvia);
        comandi.add(passo);
        comandi.add(svuota);
        JFrame f = new JFrame("Il gioco della vita");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(vista, BorderLayout.CENTER);
        f.add(comandi, BorderLayout.SOUTH);
        f.setSize(800, 660);
        return f;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VitaApp().finestra().setVisible(true));
    }
}
