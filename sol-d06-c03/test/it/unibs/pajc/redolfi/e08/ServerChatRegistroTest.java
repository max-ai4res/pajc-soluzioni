package it.unibs.pajc.redolfi.e08;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Il server senza rete: le righe del client in una stringa, il registro in un file temporaneo. */
class ServerChatRegistroTest {

    @TempDir
    Path cartella;

    private Path registro() {
        return cartella.resolve("registro.txt");
    }

    /** Le righe del server, senza l'INFO di benvenuto. */
    private List<String> risposte(String righe) throws IOException {
        StringWriter uscita = new StringWriter();
        new ServerChatRegistro(new StringReader(righe), uscita, registro()).dialoga();
        List<String> tutte = uscita.toString().lines().toList();
        assertTrue(tutte.get(0).startsWith("INFO "));
        return tutte.subList(1, tutte.size());
    }

    @Test
    void ogniMessaggioATuttiFinisceNelRegistro() throws IOException {
        List<String> righe = risposte("/nome Marco\nciao a tutti\nancora io\n");
        assertEquals(List.of("BENVENUTO Marco", "MSG Marco ciao a tutti", "MSG Marco ancora io"), righe);
        List<String> file = Files.readAllLines(registro(), StandardCharsets.UTF_8);
        assertEquals(2, file.size());
        assertTrue(file.get(0).matches("\\d\\d:\\d\\d:\\d\\d Marco ciao a tutti"), file.get(0));
        assertTrue(file.get(1).endsWith(" Marco ancora io"));
    }

    @Test
    void storiaRimandaLeUltimeRighe() throws IOException {
        List<String> righe = risposte("/nome Marco\nuno\ndue\ntre\n/storia 2\n");
        List<String> info = righe.stream().filter(r -> r.startsWith("INFO ")).toList();
        assertEquals(2, info.size());
        assertTrue(info.get(0).endsWith(" Marco due"), info.get(0));
        assertTrue(info.get(1).endsWith(" Marco tre"), info.get(1));
    }

    @Test
    void storiaPiuLungaDelRegistro() throws IOException {
        List<String> righe = risposte("/nome Marco\nuno\n/storia 10\n");
        assertEquals(1, righe.stream().filter(r -> r.startsWith("INFO ")).count());
    }

    @Test
    void registroVuoto() throws IOException {
        assertEquals(List.of("BENVENUTO Marco", "INFO (registro vuoto)"), risposte("/nome Marco\n/storia 3\n"));
        assertFalse(Files.exists(registro()));
    }

    @Test
    void nNonValido() throws IOException {
        List<String> righe = risposte("/nome Marco\n/storia tre\n/storia -2\n/storia\n");
        assertEquals(List.of("BENVENUTO Marco", "ERR n non valido: tre", "ERR n non valido: -2",
                             "ERR uso: /storia n   le ultime n righe del registro"), righe);
    }

    @Test
    void storiaPrimaDelNome() throws IOException {
        assertEquals(List.of("ERR prima scegli un nome con /nome"), risposte("/storia 3\n"));
    }

    @Test
    void ilPrivatoEIlDialogoDellaV1() throws IOException {
        assertEquals(List.of("BENVENUTO Marco", "PRIV Marco Marco mi parlo da solo",
                             "ERR utente sconosciuto: Giulia", "UTENTI Marco", "CIAO"),
                risposte("/nome Marco\n/a Marco mi parlo da solo\n/a Giulia ciao\n/chi\n/esci\nciao\n"));
    }

    @Test
    void aiutoElencaAncheStoria() throws IOException {
        List<String> righe = risposte("/aiuto\n");
        assertTrue(righe.stream().anyMatch(r -> r.startsWith("INFO /storia n")));
        assertEquals(Protocollo.COMANDI.size() + 1, righe.size());
    }
}
