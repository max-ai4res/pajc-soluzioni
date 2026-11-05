package it.unibs.pajc.redolfi.e07;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * La finestra dello studente: trova il docente in UDP (o usa l'host scritto), si
 * collega in TCP, mostra la domanda con un pulsante per risposta, vota una volta e vede
 * l'istogramma aggiornarsi a ogni voto che arriva (thread lettore + invokeLater).
 */
public class StudenteApp extends JFrame {
    private static final long serialVersionUID = 1L;

    private final JTextField txtNome = new JTextField("Marco", 8);
    private final JTextField txtHost = new JTextField(12);
    private final JButton btnEntra = new JButton("Cerca ed entra");
    private final JLabel lblDomanda = new JLabel("in attesa di una domanda");
    private final JPanel pnlRisposte = new JPanel(new FlowLayout());
    private final PnlIstogramma istogramma = new PnlIstogramma();
    private List<String> risposte = List.of();
    private PrintWriter out;

    public StudenteApp() {
        super("Sondaggio: studente");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel nord = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nord.add(new JLabel("Nome:")); nord.add(txtNome);
        nord.add(new JLabel("Docente:")); nord.add(txtHost); nord.add(btnEntra);
        JPanel centro = new JPanel(new BorderLayout());
        centro.add(lblDomanda, BorderLayout.NORTH);
        centro.add(pnlRisposte, BorderLayout.CENTER);
        add(nord, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(istogramma, BorderLayout.SOUTH);
        btnEntra.addActionListener(e -> entra());
        pack();
        setLocationRelativeTo(null);
    }

    private void entra() {
        btnEntra.setEnabled(false);
        new SwingWorker<Socket, Void>() {
            @Override protected Socket doInBackground() throws IOException {
                String host = txtHost.getText().strip();
                if (host.isEmpty()) host = cerca();
                if (host == null) throw new IOException("nessun docente ha risposto");
                return new Socket(host, Protocollo.PORTA);
            }
            @Override protected void done() {
                try { collegato(get()); }
                catch (Exception e) {
                    btnEntra.setEnabled(true);
                    JOptionPane.showMessageDialog(StudenteApp.this, e.getCause().getMessage(), "Sondaggio", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    static String cerca() throws IOException {
        try (DatagramSocket s = new DatagramSocket()) {
            s.setBroadcast(true);
            s.setSoTimeout(1500);
            byte[] d = "SONDAGGIO?".getBytes(StandardCharsets.UTF_8);
            s.send(new DatagramPacket(d, d.length, InetAddress.getByName("255.255.255.255"), Protocollo.PORTA));
            DatagramPacket p = new DatagramPacket(new byte[64], 64);
            s.receive(p);
            return p.getAddress().getHostAddress();
        } catch (SocketTimeoutException e) {
            return null;
        }
    }

    private void collegato(Socket socket) throws IOException {
        txtHost.setText(socket.getInetAddress().getHostAddress());
        out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        Thread.ofVirtual().start(() -> {
            try {
                String riga;
                while ((riga = in.readLine()) != null) {
                    String r = riga;
                    SwingUtilities.invokeLater(() -> ricevuta(r));
                }
            } catch (IOException e) { /* chiuso */ }
            SwingUtilities.invokeLater(() -> { lblDomanda.setText("connessione chiusa"); btnEntra.setEnabled(true); });
        });
        out.println("/nome " + txtNome.getText().strip());
    }

    private void ricevuta(String riga) {
        List<String> d = Protocollo.parseDomanda(riga);
        if (d != null) {
            lblDomanda.setText(d.get(0));
            risposte = d.subList(1, d.size());
            pnlRisposte.removeAll();
            for (int i = 0; i < risposte.size(); i++) {
                int k = i;
                JButton b = new JButton(risposte.get(i));
                b.addActionListener(e -> { out.println("/voto " + k); for (var c : pnlRisposte.getComponents()) c.setEnabled(false); });
                pnlRisposte.add(b);
            }
            pnlRisposte.revalidate();
            istogramma.mostra(risposte, new int[risposte.size()]);
        } else if (riga.startsWith("VOTI ")) {
            istogramma.mostra(risposte, Protocollo.parseVoti(riga));
        } else if (riga.equals("CHIUSA")) {
            lblDomanda.setText("domanda chiusa, in attesa della prossima");
            pnlRisposte.removeAll(); pnlRisposte.revalidate(); pnlRisposte.repaint();
            istogramma.mostra(List.of(), new int[0]);
        } else if (riga.startsWith("ERR ")) {
            JOptionPane.showMessageDialog(this, riga.substring(4), "Sondaggio", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudenteApp().setVisible(true));
    }
}
