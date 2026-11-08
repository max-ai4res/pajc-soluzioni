package it.unibs.pajc.redolfi.e08;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.Timer;

/** Il controllore della quinta versione: come il quarto, con lo scenario che si puo cambiare. */
public class Controllore {

    public record Registrato(double tempo, Comando comando) {
    }

    private Mondo mondo;
    private Supplier<Mondo> nuovoMondo;
    private final PnlMondo vista;
    private final List<MondoListener> listenerFissi = new ArrayList<>();
    private final Timer timer = new Timer(16, e -> stepNext());
    private long ultimo;
    private Stato stato;
    private Materiale materiale = Materiale.GOMMA;
    private final Deque<Comando> fatti = new ArrayDeque<>();
    private final List<Registrato> nastro = new ArrayList<>();

    public Controllore(Supplier<Mondo> nuovoMondo, PnlMondo vista) {
        this.nuovoMondo = nuovoMondo;
        this.mondo = vista.mondo();
        this.vista = vista;
        vista.addPuntoListener(p -> stato.punto(this, p));
        vista.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                stato.tasto(Controllore.this, e.getKeyCode());
            }
        });
        cambia(new Menu());
    }

    public Mondo mondo()             { return mondo; }
    public Materiale materiale()     { return materiale; }
    public List<Registrato> nastro() { return List.copyOf(nastro); }

    public void setMateriale(Materiale m) { materiale = m; }

    /** Un listener che deve seguire il mondo anche quando il mondo viene sostituito. */
    public void ascoltaSempre(MondoListener l) {
        listenerFissi.add(l);
        mondo.addMondoListener(l);
    }

    public void cambia(Stato nuovo) {
        stato = nuovo;
        vista.setMessaggio(nuovo.messaggio());
        nuovo.entra(this);
        vista.repaint();
    }

    public void esegui(Comando c) {
        c.esegui(mondo);
        if (c.annullabile())
            fatti.push(c);
        nastro.add(new Registrato(mondo.tempo(), c));
    }

    public void annulla() {
        if (!fatti.isEmpty())
            fatti.pop().annulla(mondo);
    }

    /** Si cambia scenario: la fabbrica nuova, un mondo nuovo, il nastro da capo. */
    public void cambiaScenario(Supplier<Mondo> fabbrica) {
        nuovoMondo = fabbrica;
        nastro.clear();
        ricomincia();
        cambia(new InCorso());
    }

    void ricomincia() {
        mondo = nuovoMondo.get();
        for (MondoListener l : listenerFissi)
            mondo.addMondoListener(l);
        vista.setMondo(mondo);
        fatti.clear();
    }

    public void avvia() {
        ultimo = System.nanoTime();
        timer.start();
    }

    private void stepNext() {
        long adesso = System.nanoTime();
        double dt = (adesso - ultimo) / 1e9;
        ultimo = adesso;
        stato.stepNext(this, dt);
        vista.repaint();
    }
}
