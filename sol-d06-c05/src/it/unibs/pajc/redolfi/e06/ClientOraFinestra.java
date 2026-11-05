package it.unibs.pajc.redolfi.e06;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Esercizio 5.6 - l'ora in finestra: un pulsante "Aggiorna" che interroga il server
 * dell'ora (sol-d06-c02, e06) e mostra la riga in un'etichetta grande.
 *
 * Connessione e lettura stanno in doInBackground di un SwingWorker: durano (DNS,
 * connect con timeout, la riga) e non devono girare sull'EDT. done() gira sull'EDT:
 * aggiorna l'etichetta, riaccende il pulsante, e con get() riceve l'eccezione del
 * lavoro incartata in una ExecutionException, la cui causa e' quella vera
 * (ConnectException, UnknownHostException, timeout): va in un JOptionPane, e il
 * programma continua.
 *
 * Errore tipico: new Socket nel listener del pulsante: con il server fermo la finestra
 * congela per tutta la durata del timeout, e con un host che non risponde per decine
 * di secondi.
 */
public class ClientOraFinestra extends JFrame {

    private static final long serialVersionUID = 1L;

    private final JTextField txtHost = new JTextField("localhost", 12);
    private final JLabel lblOra = new JLabel("--:--:--", SwingConstants.CENTER);
    private final JButton btnAggiorna = new JButton("Aggiorna");

    public ClientOraFinestra() {
        super("Ora del server");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        lblOra.setFont(new Font(Font.MONOSPACED, Font.BOLD, 56));
        lblOra.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        JPanel nord = new JPanel();
        nord.add(new JLabel("Server:"));
        nord.add(txtHost);
        nord.add(btnAggiorna);
        add(nord, BorderLayout.NORTH);
        add(lblOra, BorderLayout.CENTER);
        btnAggiorna.addActionListener(e -> aggiorna());
        pack();
        setLocationRelativeTo(null);
    }

    private void aggiorna() {
        String host = txtHost.getText().strip();
        btnAggiorna.setEnabled(false);
        lblOra.setText("...");
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws IOException {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(host, 1234), 3000);
                    socket.setSoTimeout(3000);
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            socket.getInputStream(), StandardCharsets.UTF_8));
                    String ora = in.readLine();
                    if (ora == null) throw new IOException("il server ha chiuso senza rispondere");
                    return ora;
                }
            }

            @Override
            protected void done() {
                btnAggiorna.setEnabled(true);
                try {
                    lblOra.setText(get());
                } catch (ExecutionException e) {
                    lblOra.setText("--:--:--");
                    JOptionPane.showMessageDialog(ClientOraFinestra.this,
                            "Impossibile leggere l'ora da " + host + ":\n" + e.getCause().getMessage(),
                            "Ora del server", JOptionPane.ERROR_MESSAGE);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientOraFinestra().setVisible(true));
    }
}
