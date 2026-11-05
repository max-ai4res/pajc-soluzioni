package it.unibs.pajc.redolfi.e07;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Palestra 7.7 - il sondaggio in aula: la finestra del docente, che avvia anche il
 * server. UDP per farsi trovare (SONDAGGIO?), TCP per votare. Il docente scrive la
 * domanda e le risposte separate da ";", apre e chiude; l'istogramma si aggiorna a ogni
 * voto perche' la finestra ascolta lo stesso Sondaggio delle sessioni (le righe arrivano
 * da thread delle sessioni: invokeLater).
 *
 * Errore tipico: il voto doppio contato due volte perche' "ha gia' votato?" e "conta" sono
 * due passi: putIfAbsent li fa in uno.
 */
public class DocenteApp extends JFrame {
    private static final long serialVersionUID = 1L;

    private final Sondaggio sondaggio = new Sondaggio();
    private final JTextField txtDomanda = new JTextField("Quale struttura dati per il registro?");
    private final JTextField txtRisposte = new JTextField("HashMap;ConcurrentHashMap;ArrayList;un array");
    private final PnlIstogramma istogramma = new PnlIstogramma();
    private List<String> risposte = List.of();

    public DocenteApp() throws IOException {
        super("Sondaggio: docente");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel nord = new JPanel(new GridLayout(2, 2, 6, 4));
        nord.add(new JLabel("Domanda:")); nord.add(txtDomanda);
        nord.add(new JLabel("Risposte (;):")); nord.add(txtRisposte);
        JButton btnApri = new JButton("Apri"), btnChiudi = new JButton("Chiudi");
        JPanel sud = new JPanel();
        sud.add(btnApri); sud.add(btnChiudi);
        add(nord, BorderLayout.NORTH);
        add(istogramma, BorderLayout.CENTER);
        add(sud, BorderLayout.SOUTH);
        btnApri.addActionListener(e -> {
            risposte = Arrays.stream(txtRisposte.getText().split(";")).map(String::strip).filter(s -> !s.isEmpty()).toList();
            if (risposte.size() >= 2) sondaggio.apri(txtDomanda.getText().strip(), risposte);
        });
        btnChiudi.addActionListener(e -> sondaggio.chiudi());
        sondaggio.ascolta(riga -> SwingUtilities.invokeLater(() -> ricevuta(riga)));
        Thread.ofVirtual().start(new ServerSondaggio(sondaggio));
        pack();
        setLocationRelativeTo(null);
    }

    private void ricevuta(String riga) {
        if (riga.startsWith("VOTI ")) istogramma.mostra(risposte, Protocollo.parseVoti(riga));
        else if (riga.equals("CHIUSA")) istogramma.mostra(List.of(), new int[0]);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { new DocenteApp().setVisible(true); }
            catch (IOException e) { throw new RuntimeException(e); }
        });
    }
}
