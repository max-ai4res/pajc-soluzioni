package it.unibs.pajc.redolfi.e05;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Il client in finestra del trasferimento di file (Palestra 7.5): chat minima con
 * l'elenco degli utenti, "Invia file a..." (scegli il file, /invia, poi METTI a blocchi
 * nel deposito con la barra di invio) e, quando arriva FILE, un dialogo "accettare?":
 * /accetta, poi PRENDI dal deposito a blocchi con la barra di ricezione, e il file va in
 * ricevuti/ con il solo nome (getFileName) e senza sovrascrivere. Le righe del server
 * passano da una tabella da parola ad azione, speculare a quella dei comandi sul server;
 * i comandi partono come righe composte. Due SwingWorker, uno per verso; la chat sul
 * thread lettore + invokeLater come nel capitolo 5.
 */
public class ClientFileFinestra extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final int BLOCCO = 64 * 1024;

    private final JTextField txtHost = new JTextField("localhost", 10);
    private final JTextField txtPorta = new JTextField(String.valueOf(Protocollo.PORTA), 5);
    private final JTextField txtNome = new JTextField("Marco", 8);
    private final JButton btnConnetti = new JButton("Connetti");
    private final JButton btnInvia = new JButton("Invia file a...");
    private final DefaultListModel<String> utenti = new DefaultListModel<>();
    private final JList<String> lstUtenti = new JList<>(utenti);
    private final JTextArea txtLog = new JTextArea(8, 40);
    private final JProgressBar barraInvio = new JProgressBar(0, 100);
    private final JProgressBar barraRicezione = new JProgressBar(0, 100);
    private Socket chat;
    private PrintWriter out;
    private String host;
    private int porta;
    private Path daInviare;                      // il file scelto, in attesa di CARICA

    /** La prima parola della riga e' la chiave; il resto della riga e' l'argomento. */
    private final Map<String, Consumer<String>> risposte = new HashMap<>();

    public ClientFileFinestra() {
        super("WhatsPAJC: file");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel nord = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nord.add(new JLabel("Server:")); nord.add(txtHost); nord.add(txtPorta);
        nord.add(new JLabel("Nome:")); nord.add(txtNome);
        nord.add(btnConnetti); nord.add(btnInvia);
        JPanel barre = new JPanel(new GridLayout(2, 1, 0, 4));
        barre.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        barraInvio.setStringPainted(true); barraInvio.setString("invio");
        barraRicezione.setStringPainted(true); barraRicezione.setString("ricezione");
        barre.add(barraInvio); barre.add(barraRicezione);
        txtLog.setEditable(false);
        JScrollPane scrUtenti = new JScrollPane(lstUtenti);
        scrUtenti.setBorder(BorderFactory.createTitledBorder("Utenti"));
        add(nord, BorderLayout.NORTH);
        add(scrUtenti, BorderLayout.WEST);
        add(new JScrollPane(txtLog), BorderLayout.CENTER);
        add(barre, BorderLayout.SOUTH);
        btnInvia.setEnabled(false);
        btnConnetti.addActionListener(e -> connetti());
        btnInvia.addActionListener(e -> scegliEInvia());
        tabellaRisposte();
        pack();
        setLocationRelativeTo(null);
    }

    private void tabellaRisposte() {
        risposte.put("BENVENUTO", nome -> { log("connesso come " + nome); btnInvia.setEnabled(true); out.println("/chi"); });
        risposte.put("UTENTI", elenco -> { utenti.clear(); if (!elenco.isEmpty()) for (String u : elenco.split(",")) utenti.addElement(u); });
        risposte.put("ENTRA", nome -> { if (!utenti.contains(nome)) utenti.addElement(nome); });
        risposte.put("ESCE", utenti::removeElement);
        risposte.put("MSG", resto -> {
            String[] m = Protocollo.argomenti(resto, 2);                            // da testo
            if (m != null) log(m[0] + ": " + m[1]);
        });
        risposte.put("ERR", motivo -> log("errore: " + motivo));
        risposte.put("CARICA", id -> carica((int) Protocollo.numero(id)));
        risposte.put("FILE", resto -> {
            String[] f = Protocollo.argomenti(resto, 4);                            // da nome dimensione id
            if (f == null) return;
            int scelta = JOptionPane.showConfirmDialog(this, f[0] + " ti manda " + f[1] + " (" + Protocollo.numero(f[2]) / 1024 + " KB). Accetti?",
                    "File in arrivo", JOptionPane.YES_NO_OPTION);
            if (scelta == JOptionPane.YES_OPTION) out.println("/accetta");
        });
        risposte.put("PRENDI", resto -> {
            String[] p = Protocollo.argomenti(resto, 2);                            // id nome
            if (p != null) prendi((int) Protocollo.numero(p[0]), p[1], 0);
        });
    }

    private void log(String s) { txtLog.append(s + "\n"); }

    private void connetti() {
        if (chat != null) { try { chat.close(); } catch (IOException e) { } chat = null; return; }
        host = txtHost.getText().strip();
        String nome = txtNome.getText().strip();
        try {
            porta = Integer.parseInt(txtPorta.getText().strip());
            chat = new Socket(host, porta);
            out = new PrintWriter(chat.getOutputStream(), true, StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(new InputStreamReader(chat.getInputStream(), StandardCharsets.UTF_8));
            Thread.ofVirtual().start(() -> {
                try {
                    in.lines().forEach(riga -> SwingUtilities.invokeLater(() -> ricevuta(riga)));
                } catch (UncheckedIOException e) { /* chiuso */ }
                SwingUtilities.invokeLater(() -> { log("[connessione chiusa]"); chat = null; btnInvia.setEnabled(false); btnConnetti.setText("Connetti"); });
            });
            out.println("/nome " + nome);
            btnConnetti.setText("Disconnetti");
        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Impossibile connettersi: " + e.getMessage());
        }
    }

    /** Sull'EDT: la tabella speculare decide; una parola sconosciuta (INFO, PRIV, CIAO) si ignora. */
    private void ricevuta(String riga) {
        String[] campi = riga.split(" ", 2);
        String resto = campi.length > 1 ? campi[1] : "";
        risposte.getOrDefault(campi[0], r -> { }).accept(resto);
    }

    private void scegliEInvia() {
        String dest = lstUtenti.getSelectedValue();
        if (dest == null) { JOptionPane.showMessageDialog(this, "Scegli un utente nell'elenco"); return; }
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        daInviare = fc.getSelectedFile().toPath();
        try {
            out.println("/invia " + dest + " " + daInviare.getFileName() + " " + Files.size(daInviare));
        } catch (IOException e) {
            log("errore: " + e.getMessage());
        }
    }

    /** CARICA id: il mittente mette i byte nel deposito, a blocchi, con la barra. */
    private void carica(int id) {
        Path file = daInviare;
        if (file == null || id < 0) return;
        new SwingWorker<Void, Long>() {
            long totale;
            @Override protected Void doInBackground() throws IOException {
                totale = Files.size(file);
                try (Socket s = new Socket(host, porta + 1);
                     DataOutputStream o = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
                     DataInputStream i = new DataInputStream(s.getInputStream());
                     InputStream dati = new BufferedInputStream(Files.newInputStream(file))) {
                    o.writeUTF("METTI"); o.writeInt(id); o.writeInt((int) totale);
                    byte[] b = new byte[BLOCCO]; long fatti = 0; int n;
                    while ((n = dati.read(b)) > 0) { o.write(b, 0, n); fatti += n; publish(fatti); }
                    o.flush();
                    String esito = i.readUTF();
                    if (!esito.equals("OK")) throw new IOException(esito);
                }
                return null;
            }
            @Override protected void process(List<Long> l) { barraInvio.setValue((int) (100 * l.get(l.size() - 1) / Math.max(1, totale))); }
            @Override protected void done() {
                try { get(); log("inviato " + file.getFileName()); }
                catch (Exception e) { log("invio fallito: " + e.getCause().getMessage()); }
            }
        }.execute();
    }

    /** PRENDI id nome: il destinatario scarica dal deposito, a blocchi, in ricevuti/. */
    private void prendi(int id, String nome, int tentativo) {
        if (id < 0) return;
        new SwingWorker<Path, Long>() {
            long totale;
            @Override protected Path doInBackground() throws IOException {
                Path cartella = Path.of("ricevuti");
                Files.createDirectories(cartella);
                String sicuro = Path.of(nome).getFileName().toString();          // mai cartelle
                Path dest = cartella.resolve(sicuro);
                for (int k = 1; Files.exists(dest); k++) dest = cartella.resolve(k + "-" + sicuro);   // mai sovrascrivere
                try (Socket s = new Socket(host, porta + 1);
                     DataOutputStream o = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
                     DataInputStream i = new DataInputStream(new BufferedInputStream(s.getInputStream()))) {
                    o.writeUTF("PRENDI"); o.writeInt(id); o.flush();
                    totale = i.readInt();
                    if (totale == 0) throw new IOException("il file non e' (ancora) nel deposito");
                    try (OutputStream f = new BufferedOutputStream(Files.newOutputStream(dest))) {
                        byte[] b = new byte[BLOCCO]; long fatti = 0;
                        while (fatti < totale) {
                            int n = i.read(b, 0, (int) Math.min(b.length, totale - fatti));
                            if (n < 0) throw new IOException("connessione chiusa a " + fatti + " byte");
                            f.write(b, 0, n); fatti += n; publish(fatti);
                        }
                    }
                }
                return dest;
            }
            @Override protected void process(List<Long> l) { barraRicezione.setValue((int) (100 * l.get(l.size() - 1) / Math.max(1, totale))); }
            @Override protected void done() {
                try { log("ricevuto " + get()); }
                catch (Exception e) {
                    if (tentativo < 5) {                     // il mittente sta ancora caricando: riprovo fra un secondo
                        javax.swing.Timer t = new javax.swing.Timer(1000, ev -> prendi(id, nome, tentativo + 1));
                        t.setRepeats(false); t.start();
                    } else log("ricezione fallita: " + e.getCause().getMessage());
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientFileFinestra().setVisible(true));
    }
}
