package it.unibs.pajc.redolfi.e07;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.junit.jupiter.api.Test;

import it.unibs.pajc.redolfi.e07.ServerQuiz.Quesito;

/** Il quiz senza rete: due domande fisse, le righe del client in una stringa. */
class ServerQuizTest {

    private static final List<Quesito> DUE = List.of(
            new Quesito("Qual e' la capitale d'Italia?", "Roma"),
            new Quesito("Quanto fa 7 per 8?", "56"));

    /** Le righe del server, senza l'INFO di benvenuto. */
    private List<String> risposte(List<Quesito> quesiti, String righe) throws IOException {
        StringWriter uscita = new StringWriter();
        new ServerQuiz(quesiti, new StringReader(righe), uscita).dialoga();
        List<String> tutte = uscita.toString().lines().toList();
        assertTrue(tutte.get(0).startsWith("INFO "));
        return tutte.subList(1, tutte.size());
    }

    @Test
    void unQuizIntero() throws IOException {
        assertEquals(List.of("BENVENUTO Marco", "DOMANDA 1 Qual e' la capitale d'Italia?",
                             "OK 1", "DOMANDA 2 Quanto fa 7 per 8?", "NO 56", "FINE 1"),
                risposte(DUE, "/nome Marco\n/r  roma \n/r 55\nquesto non arriva\n"));
    }

    @Test
    void classificaNonConsumaLaDomanda() throws IOException {
        assertEquals(List.of("BENVENUTO Marco", "DOMANDA 1 Qual e' la capitale d'Italia?",
                             "PUNTI 0", "OK 1", "DOMANDA 2 Quanto fa 7 per 8?"),
                risposte(DUE, "/nome Marco\n/classifica\n/r Roma\n"));
    }

    @Test
    void primaIlNome() throws IOException {
        assertEquals(List.of("ERR prima scegli un nome con /nome", "ERR prima scegli un nome con /nome"),
                risposte(DUE, "/r Roma\n/classifica\n"));
    }

    @Test
    void unaRispostaSenzaBarra() throws IOException {
        assertEquals(List.of("BENVENUTO Marco", "DOMANDA 1 Qual e' la capitale d'Italia?",
                             "ERR comando sconosciuto: Roma (i comandi cominciano con /)"),
                risposte(DUE, "/nome Marco\nRoma\n"));
    }

    @Test
    void usoSbagliato() throws IOException {
        List<String> righe = risposte(DUE, "/nome Marco\n/r\n/nome\n/classifica tutti\n");
        assertTrue(righe.contains("ERR uso: /r testo     la risposta alla domanda"));
        assertTrue(righe.contains("ERR uso: /nome N      il nome, per primo"));
        assertTrue(righe.contains("ERR uso: /classifica  i punti finora"));
    }

    @Test
    void nomeNonValidoOGiaScelto() throws IOException {
        assertEquals(List.of("ERR nome non valido: Marco Rossi", "BENVENUTO Marco",
                             "DOMANDA 1 Qual e' la capitale d'Italia?", "ERR hai gia' un nome: Marco"),
                risposte(DUE, "/nome Marco Rossi\n/nome Marco\n/nome Luca\n"));
    }

    @Test
    void esciChiudeConIlTotale() throws IOException {
        assertEquals(List.of("BENVENUTO Marco", "DOMANDA 1 Qual e' la capitale d'Italia?",
                             "OK 1", "DOMANDA 2 Quanto fa 7 per 8?", "FINE 1"),
                risposte(DUE, "/nome Marco\n/r Roma\n/esci\n/r 56\n"));
        assertEquals(List.of("FINE 0"), risposte(DUE, "/esci\n"));
    }

    @Test
    void senzaDomandeFinisceSubito() throws IOException {
        assertEquals(List.of("BENVENUTO Marco", "FINE 0"), risposte(List.of(), "/nome Marco\n"));
    }

    @Test
    void aiutoElencaLaTabella() throws IOException {
        List<String> righe = risposte(DUE, "/aiuto\n");
        assertEquals(Protocollo.COMANDI.size(), righe.size());
        for (String comando : Protocollo.COMANDI.keySet())
            assertTrue(righe.stream().anyMatch(r -> r.startsWith("INFO " + comando)), comando);
    }
}
