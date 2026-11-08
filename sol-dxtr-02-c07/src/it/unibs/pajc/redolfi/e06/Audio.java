package it.unibs.pajc.redolfi.e06;

/** Cio' che il gioco chiede all'audio. Chi lo fornisce (schede, file, silenzio) e' un dettaglio. */
public interface Audio {

    void toc(double volume);

    void pop();

    /** L'audio che non suona: per i test, i server, il volume a zero. */
    Audio MUTO = new Audio() {
        public void toc(double volume) { }
        public void pop() { }
    };
}
