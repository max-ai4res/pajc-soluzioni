package it.unibs.pajc.redolfi.e08;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * /ping senza rete, su stringhe; il silenzio invece si misura solo con un socket vero,
 * perche' e' setSoTimeout a far scadere readLine: un ServerSocket sulla porta 0 e un
 * timeout di pochi millisecondi.
 */
class SessioneTest {

    private Registro registro;

    @BeforeEach
    void nuovoRegistro() {
        registro = new Registro();
    }

    private String dialogo(String righe) throws IOException {
        StringWriter uscita = new StringWriter();
        new Sessione(new StringReader(righe), uscita, registro).dialoga();
        return uscita.toString();
    }

    @Test
    void pingRispondePongAnchePrimaDelNome() throws IOException {
        String uscita = dialogo("/ping\n/nome Marco\n/ping\n");
        assertTrue(uscita.contains("PONG"));
        assertFalse(uscita.contains("ERR"));
    }

    @Test
    void pingNonArrivaAgliAltri() {
        StringWriter uMarco = new StringWriter(), uGiulia = new StringWriter();
        Sessione marco = new Sessione(new StringReader(""), uMarco, registro);
        Sessione giulia = new Sessione(new StringReader(""), uGiulia, registro);
        marco.elabora("/nome Marco");
        giulia.elabora("/nome Giulia");
        marco.elabora("/ping");
        assertTrue(uMarco.toString().contains("PONG"));
        assertFalse(uGiulia.toString().contains("PONG"));
        assertFalse(uGiulia.toString().contains("ping"));
    }

    @Test
    void pingConArgomentiEUnErrore() throws IOException {
        assertTrue(dialogo("/ping ci sei?\n").contains("ERR uso: /ping"));
    }

    /** Apre una sessione vera su localhost con quel timeout e restituisce il socket del client. */
    private Socket sessioneConTimeout(ServerSocket server, int timeoutMs) throws IOException {
        Thread.ofVirtual().start(() -> {
            try {
                new Sessione(server.accept(), registro, timeoutMs).run();
            } catch (IOException e) {
                // il test e' finito prima
            }
        });
        return new Socket("localhost", server.getLocalPort());
    }

    @Test
    void dopoTreSilenziIlServerSaluta() throws IOException {
        try (ServerSocket server = new ServerSocket(0);
             Socket client = sessioneConTimeout(server, 50);
             BufferedReader in = new BufferedReader(new InputStreamReader(
                     client.getInputStream(), StandardCharsets.UTF_8))) {
            assertEquals("INFO Benvenuto su WhatsPAJC. Scegli un nome con /nome", in.readLine());
            assertEquals("INFO sei ancora li'?", in.readLine());         // dopo due silenzi
            assertEquals("CIAO", in.readLine());                          // dopo tre
            assertNull(in.readLine());                                    // e il socket e' chiuso
        }
    }

    @Test
    void ogniPingAzzeraIlConteggio() throws Exception {
        try (ServerSocket server = new ServerSocket(0);
             Socket client = sessioneConTimeout(server, 100);
             BufferedReader in = new BufferedReader(new InputStreamReader(
                     client.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(client.getOutputStream(), true, StandardCharsets.UTF_8)) {
            in.readLine();                                                // il benvenuto
            for (int i = 0; i < 4; i++) {                                 // 600 ms in tutto:
                Thread.sleep(150);                                        // senza i ping, CIAO a 300
                out.println("/ping");
                assertEquals("PONG", in.readLine());
            }
        }
    }
}
