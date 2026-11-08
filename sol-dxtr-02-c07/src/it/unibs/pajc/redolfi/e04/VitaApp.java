package it.unibs.pajc.redolfi.e04;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Problema 4, il gioco della vita completo.
 *
 * Idea: la griglia annuncia generazione(numero, vive) e la vista si ridisegna SU EVENTO,
 * non a ogni passo del timer; gli stati Modifica (il timer non chiede generazioni, i
 * click invertono, si posano pattern), InCorso e Pausa; la FabbricaPattern legge i
 * pattern celebri dalle risorse; la velocita' e' una proprieta' legata a uno slider nei
 * due versi; la riga di stato ascolta il modello.
 *
 * Errore tipico: repaint a ogni tick del timer per una griglia che cambia cinque volte al
 * secondo; qui il timer serve solo a decidere quando chiedere la generazione.
 */
public class VitaApp {

    interface Stato {
        default void stepNext(VitaApp app, double dt) { }
        default void click(VitaApp app, int r, int c) { }
        String nome();
    }

    static class Modifica implements Stato {
        public void click(VitaApp app, int r, int c) {
            String pattern = (String) app.cmbPattern.getSelectedItem();
            if (pattern.equals("cella"))
                app.griglia.inverti(r, c);
            else
                app.griglia.posa(FabbricaPattern.daRisorsa(pattern), r, c);
        }
        public String nome() { return "modifica"; }
    }

    static class InCorso implements Stato {
        double accumulato;
        public void stepNext(VitaApp app, double dt) {
            accumulato += dt;
            double periodo = 1 / app.griglia.velocita();
            while (accumulato >= periodo) {
                accumulato -= periodo;
                app.griglia.generazione();                 // la vista si ridisegna ascoltando
            }
        }
        public String nome() { return "in corso"; }
    }

    static class Pausa implements Stato {
        public String nome() { return "pausa"; }
    }

    final Griglia griglia = new Griglia(40, 60);
    Stato stato = new Modifica();
    final JComboBox<String> cmbPattern = new JComboBox<>(new String[] { "cella", "aliante", "navicella", "cannone" });
    final JLabel riga = new JLabel();
    final JPanel vista = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            double l = Math.min((double) getWidth() / griglia.colonne(), (double) getHeight() / griglia.righe());
            for (int r = 0; r < griglia.righe(); r++)
                for (int c = 0; c < griglia.colonne(); c++)
                    if (griglia.viva(r, c)) {
                        g.setColor(new Color(0x24549C));
                        g.fillRect((int) (c * l), (int) (r * l), (int) l - 1, (int) l - 1);
                    }
        }
    };

    void cambia(Stato nuovo) {
        stato = nuovo;
        riga.setText(testoRiga(griglia.numero(), griglia.vive()));
    }

    String testoRiga(int n, int vive) {
        return "  " + stato.nome() + "   generazione " + n + "   vive " + vive;
    }

    JFrame finestra() {
        vista.setBackground(Color.WHITE);
        griglia.addListener((n, vive) -> {                 // la vista e la riga di stato ascoltano
            vista.repaint();
            riga.setText(testoRiga(n, vive));
        });
        vista.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                double l = Math.min((double) vista.getWidth() / griglia.colonne(), (double) vista.getHeight() / griglia.righe());
                int r = (int) (e.getY() / l), c = (int) (e.getX() / l);
                if (r < griglia.righe() && c < griglia.colonne())
                    stato.click(VitaApp.this, r, c);
            }
        });
        JSlider sld = new JSlider(1, 30, 5);
        sld.addChangeListener(e -> griglia.setVelocita(sld.getValue()));
        griglia.addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals("velocita"))
                sld.setValue((int) Math.round((Double) e.getNewValue()));
        });
        JButton avvia = new JButton("Avvia"), pausa = new JButton("Pausa"), modifica = new JButton("Modifica"), svuota = new JButton("Svuota");
        avvia.addActionListener(e -> cambia(new InCorso()));
        pausa.addActionListener(e -> cambia(new Pausa()));
        modifica.addActionListener(e -> cambia(new Modifica()));
        svuota.addActionListener(e -> griglia.svuota());
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (var c : new java.awt.Component[] { avvia, pausa, modifica, svuota, new JLabel("posa:"), cmbPattern, new JLabel("gen/s"), sld })
            barra.add(c);
        JFrame f = new JFrame("Il gioco della vita");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(barra, BorderLayout.NORTH);
        f.add(vista, BorderLayout.CENTER);
        f.add(riga, BorderLayout.SOUTH);
        f.setSize(900, 700);
        cambia(new Modifica());
        long[] ultimo = { System.nanoTime() };
        new Timer(16, e -> {                               // niente repaint qui
            long adesso = System.nanoTime();
            stato.stepNext(this, (adesso - ultimo[0]) / 1e9);
            ultimo[0] = adesso;
        }).start();
        return f;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VitaApp().finestra().setVisible(true));
    }
}
