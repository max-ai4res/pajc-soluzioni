package it.unibs.pajc.redolfi.e01;

class VerdeAuto extends FaseBase {

    VerdeAuto() {
        super(20);
    }

    public Semaforo.Luce luceAuto()   { return Semaforo.Luce.VERDE; }
    public Semaforo.Luce lucePedoni() { return Semaforo.Luce.ROSSO; }

    @Override
    public void chiamata() {                          // solo il verde ascolta la chiamata
        if (tempoTrascorso() >= 5)
            durata = Math.min(durata, tempoTrascorso() + 2);
    }

    @Override
    protected Fase successiva() {
        return new Giallo();
    }
}
