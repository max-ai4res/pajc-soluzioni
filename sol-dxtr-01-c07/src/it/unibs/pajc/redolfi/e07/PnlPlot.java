package it.unibs.pajc.redolfi.e07;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.function.DoubleUnaryOperator;

import javax.swing.JPanel;

/**
 * Il grafico di una funzione in un intervallo [xMin, xMax].
 *
 * Il cuore è il passaggio fra due sistemi di coordinate: quelle della funzione (x e y reali,
 * y verso l'alto) e quelle del pannello (pixel interi, y verso il basso). px e py vanno dalle
 * prime alle seconde, screenToWorldX fa il contrario e serve al mouse. Tutto il resto si appoggia a
 * questi tre metodi.
 *
 * La funzione si campiona una volta per pixel; l'intervallo delle y si sceglie da solo, dal
 * minimo e dal massimo dei campioni. Un campione che non è un numero (sin(x)/x in 0 dà NaN)
 * interrompe la linea invece di sporcarla. Le righe della griglia cadono su valori "tondi"
 * (passo 1, 2 o 5 per una potenza di 10), qualunque sia l'intervallo.
 */
public class PnlPlot extends JPanel {

	private static final int SINISTRA = 60, DESTRA = 20, SOPRA = 20, SOTTO = 35;   // margini per le etichette
	private static final Color GRIGLIA = new Color(0xE0E0E0), ASSI = new Color(0x616161);
	private static final Color CURVA = new Color(0x1565C0);

	private DoubleUnaryOperator funzione;
	private double xMin = -10, xMax = 10;
	private double yMin = -1, yMax = 1;                // ricalcolati a ogni disegno
	private int mouseX = -1;                           // la colonna sotto il mouse, -1 se è fuori

	public PnlPlot() {
		setBackground(Color.WHITE);
		MouseAdapter listener = new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				mouseX = e.getX();
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				mouseX = -1;
				repaint();
			}
		};
		addMouseListener(listener);
		addMouseMotionListener(listener);
	}

	public void setFunzione(DoubleUnaryOperator funzione) {
		this.funzione = funzione;
		repaint();
	}

	public void setIntervallo(double xMin, double xMax) {
		if (xMin >= xMax)
			throw new IllegalArgumentException("intervallo vuoto: " + xMin + " >= " + xMax);
		this.xMin = xMin;
		this.xMax = xMax;
		repaint();
	}

	// --- da coordinate della funzione a pixel, e ritorno

	private int larghezza() {
		return getWidth() - SINISTRA - DESTRA;
	}

	private int altezza() {
		return getHeight() - SOPRA - SOTTO;
	}

	private double worldToScreenX(double x) {
		return SINISTRA + (x - xMin) / (xMax - xMin) * larghezza();
	}

	private double worldToScreenY(double y) {
		return SOPRA + (yMax - y) / (yMax - yMin) * altezza();   // y cresce verso l'alto
	}

	private double screenToWorldX(int px) {
		return xMin + (double) (px - SINISTRA) / larghezza() * (xMax - xMin);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int w = larghezza(), h = altezza();
		if (w <= 0 || h <= 0 || funzione == null)
			return;

		// 1. i campioni, uno per pixel, e l'intervallo delle y che li contiene
		double[] ys = new double[w + 1];
		double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i <= w; i++) {
			ys[i] = funzione.applyAsDouble(screenToWorldX(SINISTRA + i));
			if (Double.isFinite(ys[i])) {
				min = Math.min(min, ys[i]);
				max = Math.max(max, ys[i]);
			}
		}
		if (min > max) {                                // nessun campione valido
			min = -1;
			max = 1;
		} else if (max - min < 1e-9) {                  // funzione costante: un po' di spazio intorno
			min -= 1;
			max += 1;
		}
		double margine = (max - min) * 0.05;
		yMin = min - margine;
		yMax = max + margine;

		// 2. griglia, etichette e assi
		g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		double passoX = passoTondo(xMax - xMin), passoY = passoTondo(yMax - yMin);
		for (double x = Math.ceil(xMin / passoX) * passoX; x <= xMax; x += passoX) {
			g2.setColor(GRIGLIA);
			g2.draw(new Line2D.Double(worldToScreenX(x), SOPRA, worldToScreenX(x), SOPRA + h));
			g2.setColor(ASSI);
			String s = formato(x, passoX);
			g2.drawString(s, (float) worldToScreenX(x) - g2.getFontMetrics().stringWidth(s) / 2f, SOPRA + h + 18);
		}
		for (double y = Math.ceil(yMin / passoY) * passoY; y <= yMax; y += passoY) {
			g2.setColor(GRIGLIA);
			g2.draw(new Line2D.Double(SINISTRA, worldToScreenY(y), SINISTRA + w, worldToScreenY(y)));
			g2.setColor(ASSI);
			String s = formato(y, passoY);
			g2.drawString(s, SINISTRA - 8 - g2.getFontMetrics().stringWidth(s), (float) worldToScreenY(y) + 4);
		}
		g2.setColor(ASSI);
		g2.setStroke(new BasicStroke(1.5f));
		if (yMin <= 0 && 0 <= yMax)
			g2.draw(new Line2D.Double(SINISTRA, worldToScreenY(0), SINISTRA + w, worldToScreenY(0)));
		if (xMin <= 0 && 0 <= xMax)
			g2.draw(new Line2D.Double(worldToScreenX(0), SOPRA, worldToScreenX(0), SOPRA + h));
		g2.drawRect(SINISTRA, SOPRA, w, h);

		// 3. la curva: un tratto per ogni coppia di campioni validi consecutivi
		Path2D curva = new Path2D.Double();
		boolean pennaGiu = false;
		for (int i = 0; i <= w; i++) {
			if (!Double.isFinite(ys[i])) {
				pennaGiu = false;                       // si stacca la penna
				continue;
			}
			double x = SINISTRA + i, y = worldToScreenY(ys[i]);
			if (pennaGiu)
				curva.lineTo(x, y);
			else
				curva.moveTo(x, y);
			pennaGiu = true;
		}
		g2.setColor(CURVA);
		g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2.draw(curva);

		// 4. il punto sotto il mouse
		if (mouseX >= SINISTRA && mouseX <= SINISTRA + w) {
			double x = screenToWorldX(mouseX), y = funzione.applyAsDouble(x);
			g2.setStroke(new BasicStroke(1f));
			g2.setColor(new Color(0xEF6C00));
			g2.draw(new Line2D.Double(mouseX, SOPRA, mouseX, SOPRA + h));
			if (Double.isFinite(y)) {
				g2.fill(new Ellipse2D.Double(mouseX - 5, worldToScreenY(y) - 5, 10, 10));
				String s = String.format("x = %.3f   f(x) = %.3f", x, y);
				int ls = g2.getFontMetrics().stringWidth(s);
				g2.setColor(new Color(255, 255, 255, 220));    // un fondo, per leggerla sopra la griglia
				g2.fillRect(SINISTRA + 4, SOPRA + 3, ls + 12, 18);
				g2.setColor(new Color(0xEF6C00));
				g2.drawString(s, SINISTRA + 10, SOPRA + 16);
			}
		}
	}

	/** Un passo di griglia "tondo" (1, 2 o 5 per una potenza di 10) che dà circa 8 righe. */
	private static double passoTondo(double ampiezza) {
		double grezzo = ampiezza / 8;
		double potenza = Math.pow(10, Math.floor(Math.log10(grezzo)));
		double r = grezzo / potenza;
		return (r < 1.5 ? 1 : r < 3 ? 2 : r < 7 ? 5 : 10) * potenza;
	}

	/** Un valore della griglia con le cifre decimali che il passo richiede, senza "-0". */
	private static String formato(double v, double passo) {
		if (Math.abs(v) < passo * 1e-6)
			v = 0;
		int decimali = (int) Math.max(0, -Math.floor(Math.log10(passo)));
		return String.format("%." + decimali + "f", v);
	}
}
