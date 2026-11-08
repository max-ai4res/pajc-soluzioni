package it.unibs.pajc.redolfi.e08;

/** Il listener che suona: riceve l'Audio da usare, non lo cerca da solo. */
public class Suoni implements MondoListener {

    private final Audio audio;

    public Suoni(Audio audio) {
        this.audio = audio;
    }

    @Override
    public void rimbalzo(Rimbalzo r) {
        audio.toc(Math.min(1, r.velocita() / 10));
    }

    @Override
    public void pallinaScoppiata(Pallina p) {
        audio.pop();
    }
}
