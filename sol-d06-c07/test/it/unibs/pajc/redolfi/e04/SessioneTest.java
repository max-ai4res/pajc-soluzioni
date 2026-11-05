package it.unibs.pajc.redolfi.e04;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * La Sessione senza rete, con un Quiz vero: con dialoga() il dialogo va fino alla fine
 * dell'input; con elabora() si manda una riga alla volta a piu' giocatori vivi insieme,
 * e "via" arriva dal test come arriverebbe dalla console.
 */
class SessioneTest {

    private Quiz quiz;

    @BeforeEach
    void nuovoQuiz() {
        quiz = new Quiz(List.of(new Quiz.Quesito("Capitale d'Italia?", "Roma"),
                                new Quiz.Quesito("7 per 8?", "56")));
    }

    private String dialogo(String righe) throws IOException {
        StringWriter uscita = new StringWriter();
        new Sessione(new StringReader(righe), uscita, quiz).dialoga();
        return uscita.toString();
    }

    private Sessione giocatore(String nome, StringWriter uscita) {
        Sessione s = new Sessione(new StringReader(""), uscita, quiz);
        s.elabora("/nome " + nome);
        return s;
    }

    @Test
    void primaIlNome() throws IOException {
        assertTrue(dialogo("/r Roma\n").contains("ERR prima scegli un nome"));
    }

    @Test
    void usoSbagliatoEComandoSconosciuto() throws IOException {
        String uscita = dialogo("/nome Marco\n/r\n/boh\nRoma\n");
        assertTrue(uscita.contains("ERR uso: /r testo"));
        assertTrue(uscita.contains("ERR comando sconosciuto: /boh"));
        assertTrue(uscita.contains("ERR comando sconosciuto: Roma"));
    }

    @Test
    void rispostaSenzaDomandaAperta() throws IOException {
        assertTrue(dialogo("/nome Marco\n/r Roma\n").contains("ERR nessuna domanda aperta"));
    }

    @Test
    void dueConLoStessoNome() {
        StringWriter uMarco = new StringWriter(), uAltro = new StringWriter();
        giocatore("Marco", uMarco);
        giocatore("Marco", uAltro);
        assertTrue(uMarco.toString().contains("BENVENUTO Marco"));
        assertTrue(uAltro.toString().contains("ERR nome gia' in uso: Marco"));
    }

    @Test
    void ilPrimoGiustoPrendeIlPunto() {
        StringWriter uMarco = new StringWriter(), uGiulia = new StringWriter();
        Sessione marco = giocatore("Marco", uMarco), giulia = giocatore("Giulia", uGiulia);
        quiz.via();
        assertTrue(uMarco.toString().contains("DOMANDA 1 Capitale d'Italia?"));
        assertTrue(uGiulia.toString().contains("DOMANDA 1 Capitale d'Italia?"));
        giulia.elabora("/r Milano");
        assertTrue(uGiulia.toString().contains("NO"));
        marco.elabora("/r roma");                                   // maiuscole non contano
        assertTrue(uMarco.toString().contains("PUNTO Marco Roma"));
        assertTrue(uGiulia.toString().contains("PUNTO Marco Roma"));
        giulia.elabora("/r Roma");                                  // la domanda e' chiusa
        assertTrue(uGiulia.toString().contains("ERR nessuna domanda aperta"));
        marco.elabora("/classifica");
        assertTrue(uMarco.toString().contains("CLASSIFICA Giulia:0,Marco:1"));
    }

    @Test
    void chiEntraDopoRiceveLaDomanda() {
        giocatore("Marco", new StringWriter());
        quiz.via();
        StringWriter uSara = new StringWriter();
        giocatore("Sara", uSara);
        assertTrue(uSara.toString().contains("DOMANDA 1 Capitale d'Italia?"));
        assertTrue(uSara.toString().contains("BENVENUTO Sara"));
    }

    @Test
    void esciChiudeIlDialogo() throws IOException {
        assertFalse(dialogo("/nome Marco\n/esci\n/classifica\n").contains("CLASSIFICA"));
    }
}
