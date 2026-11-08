package it.unibs.pajc.redolfi.e08;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

/**
 * Problema 8, Palline completa.
 *
 * Idea: la quinta versione rimontata, piu' due funzioni. Il salvataggio dello scenario
 * e' la fabbrica al contrario (FabbricaScenari.aTesto scrive cio' che daTesto legge) in
 * un file scelto con JFileChooser, ricaricabile con "Apri scenario". Il grafico
 * dell'energia e' una seconda vista che ascolta il mondo (evento avanzato(tempo)) e
 * campiona ogni decimo di secondo di tempo del mondo: niente timer suo, nessuna chiamata
 * dal controllore. Quando il controllore sostituisce il mondo (scenario nuovo,
 * riproduzione) la vista si ri-registra con ascoltaSempre + setMondo.
 *
 * Errore tipico: la vista dell'energia con un timer suo (sfasato) o chiamata dal
 * controllore (che non deve conoscerla).
 */
public class PallineCompleta {

    static final String[] SCENARI = { "mensole", "pioggia", "piramide", "lunare" };

    public static void main(String[] args) {
        Audio audio;
        try {
            audio = new AudioReale();
        } catch (Exception e) {
            audio = Audio.MUTO;
        }
        Audio scelto = audio;
        SwingUtilities.invokeLater(() -> {
            PnlMondo vista = new PnlMondo(FabbricaScenari.daRisorsa(SCENARI[0]));
            Controllore controllore = new Controllore(() -> FabbricaScenari.daRisorsa(SCENARI[0]), vista);
            controllore.ascoltaSempre(new Suoni(scelto));
            PnlEnergia energia = new PnlEnergia(vista.mondo());
            controllore.ascoltaSempre(new MondoListener() {   // quando il mondo cambia, il grafico lo segue
                @Override
                public void avanzato(double tempo) {
                    if (controllore.mondo() != energia.mondoCorrente())
                        energia.setMondo(controllore.mondo());
                }
            });

            JComboBox<String> cmbScenario = new JComboBox<>(SCENARI);
            cmbScenario.addActionListener(e -> {
                String nome = (String) cmbScenario.getSelectedItem();
                controllore.cambiaScenario(() -> FabbricaScenari.daRisorsa(nome));
                vista.requestFocusInWindow();
            });
            JSlider sldGravita = new JSlider(0, 2500, 981);
            sldGravita.addChangeListener(e -> {
                if (!sldGravita.getValueIsAdjusting())
                    controllore.esegui(new CambiaGravita(sldGravita.getValue() / 100.0));
            });
            JButton btnSalva = new JButton("Salva scenario"), btnApri = new JButton("Apri scenario");
            btnSalva.addActionListener(e -> {
                JFileChooser fc = new JFileChooser();
                if (fc.showSaveDialog(vista) == JFileChooser.APPROVE_OPTION) {
                    try {
                        Files.writeString(fc.getSelectedFile().toPath(), FabbricaScenari.aTesto(controllore.mondo()));
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(vista, "Non riesco a salvare: " + ex.getMessage());
                    }
                }
                vista.requestFocusInWindow();
            });
            btnApri.addActionListener(e -> {
                JFileChooser fc = new JFileChooser();
                if (fc.showOpenDialog(vista) == JFileChooser.APPROVE_OPTION) {
                    var file = fc.getSelectedFile().toPath();
                    try {
                        FabbricaScenari.daFile(file);          // si prova a leggerlo prima di adottarlo
                        controllore.cambiaScenario(() -> FabbricaScenari.daFile(file));
                    } catch (RuntimeException ex) {
                        JOptionPane.showMessageDialog(vista, "Scenario non valido: " + ex.getMessage());
                    }
                }
                vista.requestFocusInWindow();
            });
            JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
            barra.add(new JLabel("Scenario"));
            barra.add(cmbScenario);
            barra.add(new JLabel("Gravità"));
            barra.add(sldGravita);
            barra.add(btnSalva);
            barra.add(btnApri);

            JFrame frame = new JFrame("Palline completa");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(barra, BorderLayout.NORTH);
            frame.add(vista, BorderLayout.CENTER);
            frame.add(energia, BorderLayout.SOUTH);
            frame.setSize(800, 700);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    vista.requestFocusInWindow();
                    controllore.avvia();
                }
            });
            frame.setVisible(true);
        });
    }
}
