package it.unibs.pajc.redolfi.e07;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Le stanze senza rete: tre sessioni su stringhe, un Registro vero. Con dialoga() il
 * dialogo va fino alla fine dell'input; con elabora() si manda una riga alla volta a
 * piu' sessioni vive insieme.
 */
class SessioneTest {

    private Registro registro;
    private StringWriter uMarco, uGiulia, uSara;
    private Sessione marco, giulia, sara;

    @BeforeEach
    void treInGenerale() {
        registro = new Registro();
        uMarco = new StringWriter();
        uGiulia = new StringWriter();
        uSara = new StringWriter();
        marco = new Sessione(new StringReader(""), uMarco, registro);
        giulia = new Sessione(new StringReader(""), uGiulia, registro);
        sara = new Sessione(new StringReader(""), uSara, registro);
        marco.elabora("/nome Marco");
        giulia.elabora("/nome Giulia");
        sara.elabora("/nome Sara");
    }

    private String dialogo(String righe) throws IOException {
        StringWriter uscita = new StringWriter();
        new Sessione(new StringReader(righe), uscita, registro).dialoga();
        return uscita.toString();
    }

    @Test
    void entraConfermaEAvvisaLeDueStanze() {
        sara.elabora("/entra giochi");
        assertTrue(uSara.toString().contains("STANZA giochi"));
        assertTrue(uMarco.toString().contains("ESCE Sara"));            // la vecchia stanza
        assertFalse(uSara.toString().contains("ENTRA Sara"));
        giulia.elabora("/entra giochi");
        assertTrue(uSara.toString().contains("ENTRA Giulia"));          // la nuova stanza
        assertTrue(uMarco.toString().contains("ESCE Giulia"));
    }

    @Test
    void iMessaggiRestanoNellaStanza() {
        sara.elabora("/entra giochi");
        marco.elabora("ciao a tutti");
        assertTrue(uMarco.toString().contains("MSG Marco ciao a tutti"));
        assertTrue(uGiulia.toString().contains("MSG Marco ciao a tutti"));
        assertFalse(uSara.toString().contains("MSG Marco"));
        sara.elabora("qualcuno?");
        assertTrue(uSara.toString().contains("MSG Sara qualcuno?"));     // anche al mittente
        assertFalse(uMarco.toString().contains("MSG Sara"));
    }

    @Test
    void chiElencaLaPropriaStanza() {
        sara.elabora("/entra giochi");
        marco.elabora("/chi");
        sara.elabora("/chi");
        assertTrue(uMarco.toString().contains("UTENTI Giulia,Marco"));
        assertTrue(uSara.toString().contains("UTENTI Sara"));
    }

    @Test
    void iPrivatiAttraversanoLeStanze() {
        sara.elabora("/entra giochi");
        giulia.elabora("/a Sara vieni di qua");
        assertTrue(uSara.toString().contains("PRIV Giulia Sara vieni di qua"));
        assertTrue(uGiulia.toString().contains("PRIV Giulia Sara vieni di qua"));
    }

    @Test
    void stanzeContaELaVuotaScompare() {
        sara.elabora("/entra giochi");
        sara.elabora("/stanze");
        assertTrue(uSara.toString().contains("STANZE generale:2,giochi:1"));
        sara.elabora("/entra generale");
        marco.elabora("/stanze");
        assertTrue(uMarco.toString().contains("STANZE generale:3"));
        assertFalse(uMarco.toString().contains("giochi"));
    }

    @Test
    void entraDoveSeiGia() {
        marco.elabora("/entra generale");
        assertTrue(uMarco.toString().contains("ERR sei gia' in generale"));
    }

    @Test
    void stanzaNonValidaEUsoSbagliato() throws IOException {
        assertTrue(dialogo("/nome Luca\n/entra Sala Grande\n").contains("ERR nome di stanza non valido: Sala Grande"));
        assertTrue(dialogo("/nome Anna\n/entra\n").contains("ERR uso: /entra S"));
        assertTrue(dialogo("/nome Paolo\n/stanze tutte\n").contains("ERR uso: /stanze"));
    }

    @Test
    void primaIlNome() throws IOException {
        assertTrue(dialogo("/entra giochi\n").contains("ERR prima scegli un nome"));
        assertTrue(dialogo("/stanze\n").contains("ERR prima scegli un nome"));
    }

    @Test
    void aiutoElencaAncheLeStanze() throws IOException {
        String uscita = dialogo("/aiuto\n");
        assertTrue(uscita.contains("INFO /entra S"));
        assertTrue(uscita.contains("INFO /stanze"));
    }
}
