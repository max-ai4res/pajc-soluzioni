package it.unibs.pajc.redolfi.e08;

import java.util.List;

/**
 * Una modifica al disegno, che sa farsi e disfarsi. Le tre modifiche possibili sono record
 * che lavorano sulla lista delle figure ricevuta; ciascuna ricorda l'indice a cui agisce,
 * cosi' che "disfa" rimetta le cose esattamente com'erano, anche con figure uguali.
 */
public sealed interface Modifica {

    void fai(List<Figura> figure);

    void disfa(List<Figura> figure);

    record Aggiunta(int indice, Figura figura) implements Modifica {
        public void fai(List<Figura> figure) { figure.add(indice, figura); }
        public void disfa(List<Figura> figure) { figure.remove(indice); }
    }

    record Cancellazione(int indice, Figura figura) implements Modifica {
        public void fai(List<Figura> figure) { figure.remove(indice); }
        public void disfa(List<Figura> figure) { figure.add(indice, figura); }
    }

    record Spostamento(int indice, Figura prima, Figura dopo) implements Modifica {
        public void fai(List<Figura> figure) { figure.set(indice, dopo); }
        public void disfa(List<Figura> figure) { figure.set(indice, prima); }
    }
}
