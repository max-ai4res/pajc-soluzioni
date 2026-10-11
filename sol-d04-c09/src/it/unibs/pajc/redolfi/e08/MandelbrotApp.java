package it.unibs.pajc.redolfi.e08;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Palestra 9.8: un Mandelbrot in piccolo, con rendering progressivo e annullamento.
 *
 * Stato della vista (centro e scala) confinato nell'EDT: lo leggono il listener del
 * mouse e paintComponent, e lo si copia nei compiti al momento della consegna (i
 * compiti ricevono i valori, non leggono i campi).
 *
 * A ogni cambio di vista: cancel(true) sui Future ancora in lista (i non partiti
 * spariscono dalla coda, quelli in corso ricevono interrupt), poi tre compiti nuovi a
 * passo 8, 2, 1. Ogni compito calcola in una BufferedImage propria, controlla
 * isInterrupted() a ogni riga, e consegna con invokeLater SOLO se non e' stato
 * interrotto e se la sua "generazione" e' ancora quella corrente: cosi' un'immagine
 * obsoleta non copre mai quella nuova, anche se arriva in ritardo.
 *
 * Errore visto dal vivo: Thread.interrupted() al posto di isInterrupted(): consuma la
 * bandiera, e un compito gia' annullato puo' consegnare lo stesso.
 */
public class MandelbrotApp extends JPanel {
    static final int W = 600, H = 400, MAX_ITER = 200;

    private final ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final List<Future<?>> pendenti = new ArrayList<>();      // solo EDT
    private BufferedImage immagine;                                   // solo EDT
    private double centroX = -0.6, centroY = 0, scala = 3.0 / W;     // solo EDT
    private int generazione = 0;                                      // solo EDT

    public MandelbrotApp() {
        setPreferredSize(new Dimension(W, H));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                centroX += (e.getX() - W / 2.0) * scala;
                centroY += (e.getY() - H / 2.0) * scala;
                scala /= 2;
                avvia();
            }
        });
        avvia();
    }

    private void avvia() {                                            // EDT
        for (Future<?> f : pendenti) f.cancel(true);                  // obsoleti: via
        pendenti.clear();
        int gen = ++generazione;
        double cx = centroX, cy = centroY, sc = scala;                // copie per i compiti
        for (int passo : new int[] { 8, 2, 1 })
            pendenti.add(pool.submit(() -> calcola(gen, passo, cx, cy, sc)));
    }

    private void calcola(int gen, int passo, double cx, double cy, double sc) {   // thread del pool
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        for (int py = 0; py < H; py += passo) {
            if (Thread.currentThread().isInterrupted()) return;        // legge senza azzerare
            for (int px = 0; px < W; px += passo) {
                double x0 = cx + (px - W / 2.0) * sc, y0 = cy + (py - H / 2.0) * sc;
                double x = 0, y = 0;
                int it = 0;
                while (x * x + y * y <= 4 && it < MAX_ITER) {
                    double xt = x * x - y * y + x0;
                    y = 2 * x * y + y0;
                    x = xt;
                    it++;
                }
                int rgb = it == MAX_ITER ? 0 : Color.HSBtoRGB(it / 40f, 0.8f, 1f);
                for (int dy = 0; dy < passo && py + dy < H; dy++)
                    for (int dx = 0; dx < passo && px + dx < W; dx++)
                        img.setRGB(px + dx, py + dy, rgb);
            }
        }
        if (Thread.currentThread().isInterrupted()) return;
        EventQueue.invokeLater(() -> {                                 // rientro
            if (gen != generazione) return;                            // obsoleta: non copre la nuova
            immagine = img;
            repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {                        // EDT
        super.paintComponent(g);
        if (immagine != null) g.drawImage(immagine, 0, 0, null);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            JFrame f = new JFrame("Mandelbrot in piccolo: click per ingrandire");
            MandelbrotApp pannello = new MandelbrotApp();
            f.add(pannello);
            f.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) { pannello.pool.shutdownNow(); }
            });
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.pack();
            f.setVisible(true);
        });
    }
}
