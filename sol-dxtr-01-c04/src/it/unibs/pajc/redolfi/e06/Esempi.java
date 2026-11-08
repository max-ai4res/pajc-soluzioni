package it.unibs.pajc.redolfi.e06;

/** Classi di prova con zero, uno o due autori, anche sui metodi. */
public class Esempi {

    @Autore(nome = "Ada", anno = 2024)
    @Autore(nome = "Bob", anno = 2025)
    public static class Parser {
        @Autore(nome = "Carla", anno = 2026)
        public void analizza() { }
        public void aiuto() { }
    }

    @Autore(nome = "Ada", anno = 2023)
    public static class Stampante {
        @Autore(nome = "Ada", anno = 2023)
        @Autore(nome = "Carla", anno = 2025)
        public void stampa() { }
    }

    public static class Anonima {
        public void niente() { }
    }
}
