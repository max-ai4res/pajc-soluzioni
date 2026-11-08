package it.unibs.pajc.redolfi.e01;

class RossoAuto extends FaseBase {

    RossoAuto() {
        super(12);
    }

    public Semaforo.Luce luceAuto()   { return Semaforo.Luce.ROSSO; }
    public Semaforo.Luce lucePedoni() { return Semaforo.Luce.VERDE; }

    @Override
    protected Fase successiva() {
        return new VerdeAuto();
    }
}
