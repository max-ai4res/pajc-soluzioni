package it.unibs.pajc.redolfi.e09;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;

/**
 * Esercizio 3.9 - un mini-HTTP: risponde al browser su http://localhost:1234/ora.
 *
 * HTTP e' un protocollo di testo a passo: una riga di richiesta ("GET /ora HTTP/1.1"),
 * le intestazioni, una riga vuota. La risposta ha la stessa forma: riga di stato,
 * intestazioni, riga vuota, corpo. Tre cose da non sbagliare: le righe terminano con
 * \r\n; Content-Length conta i BYTE del corpo in UTF-8 (per questo il corpo si codifica
 * prima in byte[]); "Connection: close" e la chiusura dicono al browser che il corpo e'
 * finito. Il corpo e' scritto come byte sull'OutputStream, non con un PrintWriter.
 * Un client alla volta: per un browser che apre piu' connessioni serve il capitolo 4.
 *
 * Errore tipico: Content-Length = corpo.length() (caratteri): con una lettera accentata
 * il browser aspetta un byte che non arriva, o tronca l'ultimo.
 */
public class ServerHttp {

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(1234)) {
            System.out.println("Apri http://localhost:1234/ora nel browser");
            while (true) {
                try (Socket socket = server.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(
                             socket.getInputStream(), StandardCharsets.UTF_8));
                     OutputStream out = socket.getOutputStream()) {
                    String richiesta = in.readLine();              // GET /ora HTTP/1.1
                    if (richiesta == null) continue;
                    String riga;
                    while ((riga = in.readLine()) != null && !riga.isEmpty())
                        ;                                          // le intestazioni: ignorate
                    String[] c = richiesta.split(" ");
                    String percorso = c.length > 1 ? c[1] : "/";
                    System.out.println(socket.getRemoteSocketAddress() + " " + richiesta);
                    if (percorso.equals("/ora"))
                        rispondi(out, "200 OK", "<h1>Sono le " + LocalTime.now().withNano(0) + "</h1>");
                    else
                        rispondi(out, "404 Not Found", "<h1>Non c'&egrave; niente in " + percorso + "</h1>");
                } catch (IOException e) {
                    System.err.println("client perso: " + e.getMessage());
                }
            }
        }
    }

    static void rispondi(OutputStream out, String stato, String html) throws IOException {
        byte[] corpo = ("<!doctype html><html><meta charset=\"utf-8\"><body>" + html
                + "</body></html>").getBytes(StandardCharsets.UTF_8);
        String testa = "HTTP/1.1 " + stato + "\r\n"
                + "Content-Type: text/html; charset=utf-8\r\n"
                + "Content-Length: " + corpo.length + "\r\n"
                + "Connection: close\r\n"
                + "\r\n";
        out.write(testa.getBytes(StandardCharsets.US_ASCII));
        out.write(corpo);
        out.flush();
    }
}
