package it.unibs.pajc.redolfi.e09;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Esercizio 6.9 - un server HTTP minimo che serve i file di una cartella
 * (res/e09 per default): GET /nome risponde con il file e il Content-Type
 * dall'estensione, GET / con l'elenco dei file come pagina con i link, 404 se non c'e',
 * 403 se il percorso tenta di uscire dalla cartella. Un thread virtuale per richiesta.
 *
 * La sicurezza sta in una riga: il percorso richiesto, decodificato (%20) e risolto
 * contro la cartella base, viene normalizzato e DEVE cominciare con la base; altrimenti
 * 403, prima di aprire qualunque cosa. Content-Length conta i byte del file, letti con
 * Files.readAllBytes (per file grandi si scriverebbe a blocchi).
 *
 * Errore tipico: Path.of(base, percorso) senza normalize/startsWith: "GET /../../etc/passwd"
 * esce dalla cartella.
 */
public class ServerFile {

    static final Map<String, String> TIPI = Map.of(
            "html", "text/html; charset=utf-8", "htm", "text/html; charset=utf-8",
            "txt", "text/plain; charset=utf-8", "css", "text/css; charset=utf-8",
            "png", "image/png", "jpg", "image/jpeg", "jpeg", "image/jpeg", "gif", "image/gif");

    private final Path base;

    ServerFile(Path base) { this.base = base.toAbsolutePath().normalize(); }

    public static void main(String[] args) throws IOException {
        ServerFile sf = new ServerFile(Path.of(args.length > 0 ? args[0] : "res/e09"));
        try (ServerSocket server = new ServerSocket(1234)) {
            System.out.println("Servo " + sf.base + " su http://localhost:1234/");
            while (true) {
                Socket socket = server.accept();
                Thread.ofVirtual().start(() -> sf.servi(socket));
            }
        }
    }

    void servi(Socket socket) {
        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             OutputStream out = socket.getOutputStream()) {
            String richiesta = in.readLine();
            if (richiesta == null) return;
            String riga;
            while ((riga = in.readLine()) != null && !riga.isEmpty())
                ;
            String[] c = richiesta.split(" ");
            if (c.length < 2 || !c[0].equals("GET")) { rispondi(out, "400 Bad Request", "text/plain", "solo GET".getBytes(StandardCharsets.UTF_8)); return; }
            String percorso = URLDecoder.decode(c[1].split("\\?")[0], StandardCharsets.UTF_8);
            System.out.println(socket.getRemoteSocketAddress() + " GET " + percorso);
            if (percorso.equals("/")) { rispondi(out, "200 OK", "text/html; charset=utf-8", elenco()); return; }
            Path file = base.resolve(percorso.substring(1)).normalize();
            if (!file.startsWith(base)) { rispondi(out, "403 Forbidden", "text/plain", "fuori dalla cartella".getBytes(StandardCharsets.UTF_8)); return; }
            if (!Files.isRegularFile(file)) { rispondi(out, "404 Not Found", "text/plain", ("non c'e': " + percorso).getBytes(StandardCharsets.UTF_8)); return; }
            String nome = file.getFileName().toString();
            String ext = nome.contains(".") ? nome.substring(nome.lastIndexOf('.') + 1).toLowerCase() : "";
            rispondi(out, "200 OK", TIPI.getOrDefault(ext, "application/octet-stream"), Files.readAllBytes(file));
        } catch (IOException e) {
            System.out.println("connessione persa: " + e.getMessage());
        }
    }

    byte[] elenco() throws IOException {
        StringBuilder html = new StringBuilder("<!doctype html><meta charset=\"utf-8\"><h1>File</h1><ul>");
        try (Stream<Path> s = Files.list(base)) {
            s.filter(Files::isRegularFile).map(p -> p.getFileName().toString()).sorted()
             .forEach(n -> html.append("<li><a href=\"/").append(n).append("\">").append(n).append("</a></li>"));
        }
        return html.append("</ul>").toString().getBytes(StandardCharsets.UTF_8);
    }

    static void rispondi(OutputStream out, String stato, String tipo, byte[] corpo) throws IOException {
        String testa = "HTTP/1.1 " + stato + "\r\nContent-Type: " + tipo + "\r\nContent-Length: " + corpo.length
                + "\r\nConnection: close\r\n\r\n";
        out.write(testa.getBytes(StandardCharsets.US_ASCII));
        out.write(corpo);
        out.flush();
    }
}
