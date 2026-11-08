package it.unibs.pajc.redolfi.e08;

/**
 * Il materiale di una pallina (Strategy, capitolo 5). I tre materiali sono immutabili e
 * senza stato proprio: ne basta un'istanza ciascuno, condivisa da tutte le palline
 * (Flyweight). Le costanti e la fabbrica daNome() sono il modo di ottenerle.
 */
public sealed interface Materiale permits Gomma, Piombo, Bolla {

    Materiale GOMMA = new Gomma();
    Materiale PIOMBO = new Piombo();
    Materiale BOLLA = new Bolla();

    double restituzione();

    double densita();

    default boolean sopravvive(double impatto) {
        return true;
    }

    /** La fabbrica: dal nome scritto in un file al materiale condiviso. */
    static Materiale daNome(String nome) {
        return switch (nome.toLowerCase()) {
            case "gomma" -> GOMMA;
            case "piombo" -> PIOMBO;
            case "bolla" -> BOLLA;
            default -> throw new IllegalArgumentException(
                    "materiale sconosciuto: " + nome);
        };
    }
}
