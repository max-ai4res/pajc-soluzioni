package it.unibs.pajc.redolfi.e01;

class Giallo extends FaseBase {

    Giallo() {
        super(3);
    }

    public Semaforo.Luce luceAuto()   { return Semaforo.Luce.GIALLO; }
    public Semaforo.Luce lucePedoni() { return Semaforo.Luce.ROSSO; }

    @Override
    protected Fase successiva() {
        return new RossoAuto();
    }
}
