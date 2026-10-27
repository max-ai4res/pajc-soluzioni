package it.unibs.pajc.redolfi.e07;

import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

/**
 * Esercizio 3.7 - Celsius e Fahrenheit sincronizzati.
 *
 * Ogni spinner, quando cambia, imposta l'altro con setValue; setValue fa partire il
 * ChangeListener dell'altro, che senza precauzioni reimposterebbe il primo, arrotondato.
 * L'utente scrive 100,1 F: C diventa 37,8 (37,83 arrotondato), e il suo listener riporta
 * F a 100,0, cancellando il valore appena scritto. La bandiera "aggiornando" dice "questo
 * cambiamento lo sto facendo io": il listener dell'altro spinner la vede e non reagisce.
 * Tutto gira sul thread degli eventi, quindi basta un boolean, senza sincronizzazione.
 *
 * Errore tipico: senza la bandiera, il valore appena scritto cambia sotto le dita
 * dell'utente; con setValue dentro un ciclo di aggiornamenti reciproci, nel caso peggiore,
 * un rimbalzo continuo fra due valori arrotondati.
 */
public class Gradi extends JFrame {

    private static final long serialVersionUID = 1L;

    private final JSpinner spnC = spinner(20.0);
    private final JSpinner spnF = spinner(68.0);
    private boolean aggiornando;

    public Gradi() {
        super("Gradi");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new FlowLayout(FlowLayout.CENTER, 8, 12));
        add(spnC);
        add(new JLabel("°C  ="));
        add(spnF);
        add(new JLabel("°F"));

        spnC.addChangeListener(e -> {
            if (aggiornando)
                return;
            double c = (Double) spnC.getValue();
            imposta(spnF, c * 9 / 5 + 32);
        });
        spnF.addChangeListener(e -> {
            if (aggiornando)
                return;
            double f = (Double) spnF.getValue();
            imposta(spnC, (f - 32) * 5 / 9);
        });

        pack();
        setLocationRelativeTo(null);
    }

    /** Imposta l'altro spinner, arrotondato a un decimale, senza farlo reagire. */
    private void imposta(JSpinner s, double v) {
        aggiornando = true;
        try {
            s.setValue(Math.round(v * 10) / 10.0);
        } finally {
            aggiornando = false;
        }
    }

    private static JSpinner spinner(double iniziale) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(iniziale, -500.0, 1000.0, 0.1));
        s.setEditor(new JSpinner.NumberEditor(s, "0.0"));
        ((JSpinner.DefaultEditor) s.getEditor()).getTextField().setColumns(6);
        return s;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Gradi().setVisible(true));
    }
}
