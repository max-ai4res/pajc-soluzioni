package it.unibs.pajc.redolfi.e01;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Esercizio 2.1, un contatore in tre parti.
 *
 * Idea: il numero sta in un posto solo, il modello Contatore; le due viste (etichetta e
 * barra) lo leggono quando il controllore chiede loro di aggiornarsi; i tre pulsanti sono
 * il controllore, che chiama il modello e poi le viste. Il modello non importa niente di
 * Swing e si prova da solo (ProvaContatore).
 *
 * Errore tipico: tenere il valore nell'etichetta (o in un campo del pannello) e
 * "aggiornare il modello da li'": alla prima regola non banale, come il limite a 100, le
 * due viste mostrano numeri diversi.
 */
public class ContatoreApp {

    private final Contatore contatore = new Contatore();
    private final VistaNumero numero = new VistaNumero();
    private final VistaBarra barra = new VistaBarra();

    private void aggiornaViste() {
        numero.aggiorna(contatore);
        barra.aggiorna(contatore);
    }

    private JButton pulsante(String testo, Runnable azione) {
        JButton b = new JButton(testo);
        b.addActionListener(e -> {
            azione.run();                  // prima il modello...
            aggiornaViste();               // ...poi le viste lo rileggono
        });
        return b;
    }

    private JFrame finestra() {
        JPanel comandi = new JPanel(new FlowLayout());
        comandi.add(pulsante("-", contatore::decrementa));
        comandi.add(pulsante("Azzera", contatore::azzera));
        comandi.add(pulsante("+", contatore::incrementa));

        JFrame f = new JFrame("Contatore");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(numero, BorderLayout.CENTER);
        f.add(barra, BorderLayout.NORTH);
        f.add(comandi, BorderLayout.SOUTH);
        f.setSize(320, 220);
        aggiornaViste();
        return f;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ContatoreApp().finestra().setVisible(true));
    }
}
