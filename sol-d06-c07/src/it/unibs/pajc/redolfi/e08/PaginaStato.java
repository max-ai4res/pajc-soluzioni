package it.unibs.pajc.redolfi.e08;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/** La pagina di stato: un server HTTP minimo che risponde a GET /stato con chi c'e'. */
public class PaginaStato implements Runnable {
    private final ServerSocket server;
    private final Registro registro;

    public PaginaStato(int porta, Registro registro) throws IOException {
        server = new ServerSocket(porta);
        this.registro = registro;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket s = server.accept();
                Thread.ofVirtual().start(() -> servi(s));
            }
        } catch (IOException e) {
            System.out.println("pagina di stato: " + e.getMessage());
        }
    }

    private void servi(Socket socket) {
        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(
                     socket.getInputStream(), StandardCharsets.UTF_8));
             OutputStream out = socket.getOutputStream()) {
            String richiesta = in.readLine();                      // GET /stato HTTP/1.1
            if (richiesta == null) return;
            String riga;
            while ((riga = in.readLine()) != null && !riga.isEmpty())
                ;                                                  // le intestazioni
            String percorso = richiesta.split(" ").length > 1 ? richiesta.split(" ")[1] : "/";
            if (percorso.equals("/stato")) {
                StringBuilder html = new StringBuilder("<h1>WhatsPAJC</h1><p>Collegati: ")
                        .append(registro.quanti()).append("</p><ul>");
                for (String n : registro.nomi()) html.append("<li>").append(n).append("</li>");
                rispondi(out, "200 OK", "text/html; charset=utf-8", html.append("</ul>").toString());
            } else {
                rispondi(out, "404 Not Found", "text/plain; charset=utf-8", "niente in " + percorso);
            }
        } catch (IOException e) {
            // il browser ha chiuso
        }
    }

    static void rispondi(OutputStream out, String stato, String tipo, String corpo) throws IOException {
        byte[] b = corpo.getBytes(StandardCharsets.UTF_8);
        String testa = "HTTP/1.1 " + stato + "\r\nContent-Type: " + tipo + "\r\nContent-Length: "
                + b.length + "\r\nConnection: close\r\n\r\n";
        out.write(testa.getBytes(StandardCharsets.US_ASCII));
        out.write(b);
        out.flush();
    }

    public void chiudi() throws IOException { server.close(); }
}
