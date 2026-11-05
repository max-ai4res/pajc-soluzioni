package it.unibs.pajc.redolfi.e07;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Esercizio 6.7 - il trasferimento con la barra: manda un file al server degli
 * allegati di WhatsPAJC (sol-d06-c07, porta 1235: METTI utente nome + cornice) a blocchi
 * di 64 KB, con l'avanzamento in una JProgressBar e un pulsante Annulla.
 *
 * La cornice dichiara la lunghezza totale PRIMA dei byte (Files.size), poi i byte
 * partono a blocchi: dopo ogni blocco il worker chiama publish con i byte mandati e
 * process aggiorna la barra sull'EDT. Annulla chiama cancel(true) e chiude il socket:
 * il worker, che controlla isCancelled a ogni blocco, esce; il server resta in
 * readFully fino alla chiusura e scarta la foto incompleta (EOFException).
 *
 * Errore tipico: aggiornare la barra da doInBackground (thread del worker) invece che
 * da process; oppure leggere tutto il file in memoria con readAllBytes e mandarlo in
 * una volta: nessun avanzamento e un file da un GB in memoria.
 */
public class TrasferimentoFinestra extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final int BLOCCO = 64 * 1024;

    private final JTextField txtHost = new JTextField("localhost", 10);
    private final JTextField txtNome = new JTextField("Marco", 8);
    private final JButton btnScegli = new JButton("Scegli e invia...");
    private final JButton btnAnnulla = new JButton("Annulla");
    private final JProgressBar barra = new JProgressBar(0, 100);
    private final JLabel lblStato = new JLabel(" ");
    private SwingWorker<Void, Long> worker;
    private Socket socket;

    public TrasferimentoFinestra() {
        super("Invia un file a WhatsPAJC");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel nord = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nord.add(new JLabel("Server:"));
        nord.add(txtHost);
        nord.add(new JLabel("Utente:"));
        nord.add(txtNome);
        nord.add(btnScegli);
        nord.add(btnAnnulla);
        barra.setStringPainted(true);
        add(nord, BorderLayout.NORTH);
        add(barra, BorderLayout.CENTER);
        add(lblStato, BorderLayout.SOUTH);
        btnAnnulla.setEnabled(false);
        btnScegli.addActionListener(e -> scegli());
        btnAnnulla.addActionListener(e -> annulla());
        pack();
        setSize(520, getHeight());
        setLocationRelativeTo(null);
    }

    private void scegli() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        invia(fc.getSelectedFile().toPath());
    }

    private void invia(Path file) {
        String host = txtHost.getText().strip(), utente = txtNome.getText().strip();
        btnScegli.setEnabled(false);
        btnAnnulla.setEnabled(true);
        barra.setValue(0);
        worker = new SwingWorker<Void, Long>() {
            long totale;

            @Override
            protected Void doInBackground() throws IOException {
                totale = Files.size(file);
                socket = new Socket(host, 1235);
                try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                     DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                     InputStream dati = new BufferedInputStream(Files.newInputStream(file))) {
                    out.writeUTF("METTI");
                    out.writeUTF(utente);
                    out.writeUTF(file.getFileName().toString());
                    out.writeInt((int) totale);                      // la lunghezza, prima
                    byte[] blocco = new byte[BLOCCO];
                    long mandati = 0;
                    int n;
                    while (!isCancelled() && (n = dati.read(blocco)) > 0) {
                        out.write(blocco, 0, n);
                        mandati += n;
                        publish(mandati);
                    }
                    out.flush();
                    if (isCancelled()) return null;
                    String esito = in.readUTF();
                    if (!esito.equals("OK")) throw new IOException(esito);
                } finally {
                    socket.close();
                }
                return null;
            }

            @Override
            protected void process(List<Long> avanzamenti) {        // sull'EDT
                long mandati = avanzamenti.get(avanzamenti.size() - 1);
                barra.setValue((int) (100 * mandati / Math.max(1, totale)));
                lblStato.setText(" " + mandati / 1024 + " / " + totale / 1024 + " KB");
            }

            @Override
            protected void done() {
                btnScegli.setEnabled(true);
                btnAnnulla.setEnabled(false);
                if (isCancelled()) { lblStato.setText(" Annullato"); return; }
                try {
                    get();
                    barra.setValue(100);
                    lblStato.setText(" Inviato " + file.getFileName());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(TrasferimentoFinestra.this,
                            "Invio fallito: " + e.getCause().getMessage(), "Invia", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void annulla() {
        if (worker != null) worker.cancel(true);
        try { if (socket != null) socket.close(); } catch (IOException e) { /* gia' chiuso */ }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TrasferimentoFinestra().setVisible(true));
    }
}
