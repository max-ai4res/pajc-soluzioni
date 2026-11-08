package it.unibs.pajc.redolfi.e04;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.DoubleBinaryOperator;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Esercizio 7.4: la calcolatrice grafica che impara operatori nuovi.
 *
 * Idea: ogni operatore e' un DoubleBinaryOperator in una mappa simbolo -> operatore,
 * e la tastiera degli operatori si ricostruisce dalla mappa (un tasto per voce). La
 * macchina a stati della calcolatrice e' minima: primo operando, operatore scelto,
 * secondo operando, "=". "Nuovo operatore" apre il dialogo modale, che restituisce
 * l'operatore compilato solo se compila; il corpo si salva in operatori/<simbolo>.txt
 * (con il nome sulla prima riga) e all'avvio i file si ricompilano in un SwingWorker,
 * cosi' la finestra compare subito.
 *
 * Errore tipico: chiudere il dialogo su OK e mostrare l'errore dopo (l'utente riscrive
 * tutto); e compilare nel listener, bloccando l'interfaccia.
 */
public class CalcolatriceApp {

    private static final Path CARTELLA = Path.of("operatori");

    private final JFrame frame = new JFrame("Calcolatrice che impara");
    private final JLabel display = new JLabel("0", SwingConstants.RIGHT);
    private final JPanel pnlOperatori = new JPanel(new GridLayout(0, 2, 4, 4));
    private final Map<String, DoubleBinaryOperator> operatori = new LinkedHashMap<>();
    private final Map<String, String> corpi = new LinkedHashMap<>();           // simbolo -> nome\ncorpo
    private CompilatoreOperatori compilatore;

    private double primo;
    private DoubleBinaryOperator inAttesa;
    private boolean nuovoNumero = true;

    public CalcolatriceApp() {
        operatori.put("+", (a, b) -> a + b);
        operatori.put("-", (a, b) -> a - b);
        operatori.put("*", (a, b) -> a * b);
        operatori.put("/", (a, b) -> a / b);
        display.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
        JPanel cifre = new JPanel(new GridLayout(4, 3, 4, 4));
        for (String t : "7 8 9 4 5 6 1 2 3 0 . =".split(" ")) {
            JButton b = new JButton(t);
            b.addActionListener(e -> premuto(t));
            cifre.add(b);
        }
        ricostruisciTastiera();
        JButton btnNuovo = new JButton("Nuovo operatore...");
        btnNuovo.addActionListener(e -> nuovoOperatore());
        frame.add(display, BorderLayout.NORTH);
        frame.add(cifre, BorderLayout.CENTER);
        frame.add(pnlOperatori, BorderLayout.EAST);
        frame.add(btnNuovo, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(420, 360);
        try {
            compilatore = new CompilatoreOperatori();
            ricaricaSalvati();
        } catch (IllegalStateException | IOException e) {
            JOptionPane.showMessageDialog(frame, e.getMessage() + "\nGli operatori nuovi non sono disponibili.",
                    "Compilatore", JOptionPane.WARNING_MESSAGE);
            btnNuovo.setEnabled(false);
        }
    }

    private void ricostruisciTastiera() {
        pnlOperatori.removeAll();
        for (String simbolo : operatori.keySet()) {
            JButton b = new JButton(simbolo);
            b.setToolTipText(corpi.getOrDefault(simbolo, simbolo));
            b.addActionListener(e -> operatore(operatori.get(simbolo)));
            pnlOperatori.add(b);
        }
        pnlOperatori.revalidate();
        pnlOperatori.repaint();
    }

    private void premuto(String tasto) {
        if (tasto.equals("=")) {
            if (inAttesa != null) {
                display.setText(formatta(inAttesa.applyAsDouble(primo, valore())));
                inAttesa = null;
            }
            nuovoNumero = true;
        } else {
            display.setText(nuovoNumero ? tasto : display.getText() + tasto);
            nuovoNumero = false;
        }
    }

    private void operatore(DoubleBinaryOperator op) {
        if (inAttesa != null && !nuovoNumero)
            display.setText(formatta(inAttesa.applyAsDouble(primo, valore())));
        primo = valore();
        inAttesa = op;
        nuovoNumero = true;
    }

    private double valore() {
        try {
            return Double.parseDouble(display.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String formatta(double v) {
        return v == Math.rint(v) && Math.abs(v) < 1e15 ? String.valueOf((long) v) : String.valueOf(v);
    }

    private void nuovoOperatore() {
        new DlgNuovoOperatore(frame, compilatore).chiedi().ifPresent(op -> {
            operatori.put(op.simbolo(), op.op());
            corpi.put(op.simbolo(), op.nome() + "\n" + op.corpo());
            ricostruisciTastiera();
            try {
                Files.createDirectories(CARTELLA);
                Files.writeString(CARTELLA.resolve(op.simbolo() + ".txt"), op.nome() + "\n" + op.corpo() + "\n");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "operatore non salvato: " + e.getMessage());
            }
        });
    }

    /** All'avvio: i corpi salvati, ricompilati fuori dall'EDT e aggiunti man mano. */
    private void ricaricaSalvati() throws IOException {
        if (!Files.isDirectory(CARTELLA))
            return;
        try (Stream<Path> file = Files.list(CARTELLA)) {
            for (Path p : file.filter(f -> f.toString().endsWith(".txt")).toList()) {
                String simbolo = p.getFileName().toString().replace(".txt", "");
                String testo = Files.readString(p);
                String corpo = testo.substring(testo.indexOf('\n') + 1);
                new javax.swing.SwingWorker<DoubleBinaryOperator, Void>() {
                    @Override protected DoubleBinaryOperator doInBackground() throws Exception {
                        return compilatore.compila(corpo);
                    }
                    @Override protected void done() {
                        try {
                            operatori.put(simbolo, get());
                            corpi.put(simbolo, testo.strip());
                            ricostruisciTastiera();
                        } catch (Exception e) {
                            System.out.println("operatore " + simbolo + " non ricompilato: " + e.getCause());
                        }
                    }
                }.execute();
            }
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new CalcolatriceApp().frame.setVisible(true));
    }
}
