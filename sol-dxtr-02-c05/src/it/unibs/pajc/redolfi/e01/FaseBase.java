package it.unibs.pajc.redolfi.e01;

/** Lo scheletro di una fase: conta il tempo e passa alla successiva allo scadere (un Template Method). */
abstract class FaseBase implements Fase {

    protected double durata;
    private double trascorso;

    FaseBase(double durata) {
        this.durata = durata;
    }

    @Override
    public double tempoTrascorso() {
        return trascorso;
    }

    @Override
    public Fase stepNext(double dt) {
        trascorso += dt;
        return trascorso >= durata ? successiva() : this;      // una fase nuova ogni volta: il tempo riparte
    }

    protected abstract Fase successiva();
}
