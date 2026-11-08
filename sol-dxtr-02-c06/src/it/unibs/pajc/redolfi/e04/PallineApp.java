package it.unibs.pajc.redolfi.e04;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;

/**
 * Esercizio 6.4, un tema a caldo.
 *
 * Idea: Tema e' un record immutabile letto da un file di proprieta' e condiviso (una
 * cache nella fabbrica daNome: Flyweight); Disegnatore e' la Strategy che sa disegnare
 * pallina e scheggia con un tema; il tema e' una proprieta' della vista, annunciata con
 * PropertyChangeSupport; il menu la cambia e il pannello si ridisegna. Modello e
 * controllore non sanno che i temi esistono.
 *
 * Errore tipico: un campo colore nella pallina impostato dal tema corrente: al cambio di
 * tema le palline vecchie restano del colore vecchio, e il modello ha un colore.
 */
public class PallineApp {

    static final String[] SCENARI = { "mensole", "pioggia", "piramide", "lunare" };
    static final String[] TEMI = { "classico", "notte", "carta" };

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PnlMondo vista = new PnlMondo(FabbricaScenari.daRisorsa(SCENARI[0]));
            Controllore controllore = new Controllore(() -> FabbricaScenari.daRisorsa(SCENARI[0]), vista);
            controllore.ascoltaSempre(new Suoni(Audio.MUTO));

            JComboBox<String> cmbScenario = new JComboBox<>(SCENARI);
            cmbScenario.addActionListener(e -> {
                String nome = (String) cmbScenario.getSelectedItem();
                controllore.cambiaScenario(() -> FabbricaScenari.daRisorsa(nome));
                vista.requestFocusInWindow();
            });
            JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
            barra.add(new JLabel("Scenario"));
            barra.add(cmbScenario);
            JLabel lblTema = new JLabel("tema: classico");
            barra.add(lblTema);
            vista.addPropertyChangeListener(e -> {       // chi vuole, ascolta il tema
                if (e.getPropertyName().equals("tema"))
                    lblTema.setText("tema: " + ((Tema) e.getNewValue()).nome());
            });

            JMenu menuTema = new JMenu("Tema");
            ButtonGroup gruppo = new ButtonGroup();
            for (String t : TEMI) {
                JRadioButtonMenuItem voce = new JRadioButtonMenuItem(t, t.equals("classico"));
                voce.addActionListener(e -> vista.setTema(Tema.daNome(t)));
                gruppo.add(voce);
                menuTema.add(voce);
            }
            JMenuBar menu = new JMenuBar();
            menu.add(menuTema);

            JFrame frame = new JFrame("Palline con i temi");
            frame.setJMenuBar(menu);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(barra, BorderLayout.NORTH);
            frame.add(vista, BorderLayout.CENTER);
            frame.setSize(760, 560);
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
