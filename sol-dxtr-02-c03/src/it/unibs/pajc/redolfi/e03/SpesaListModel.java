package it.unibs.pajc.redolfi.e03;

import javax.swing.AbstractListModel;

/**
 * Il ListModel della JList: non tiene niente, legge da Spesa, e traduce i fatti di Spesa
 * negli eventi che la JList capisce. Due liste con una sola verita'.
 */
public class SpesaListModel extends AbstractListModel<Voce> implements SpesaListener {

    private final Spesa spesa;

    public SpesaListModel(Spesa spesa) {
        this.spesa = spesa;
        spesa.addSpesaListener(this);
    }

    @Override
    public int getSize() {
        return spesa.quante();
    }

    @Override
    public Voce getElementAt(int i) {
        return spesa.voce(i);
    }

    @Override
    public void aggiunta(int i, Voce v) {
        fireIntervalAdded(this, i, i);
    }

    @Override
    public void rimossa(int i, Voce v) {
        fireIntervalRemoved(this, i, i);
    }

    @Override
    public void cambiata(int i, Voce v) {
        fireContentsChanged(this, i, i);
    }
}
