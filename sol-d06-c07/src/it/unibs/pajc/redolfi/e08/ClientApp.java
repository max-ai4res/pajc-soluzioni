package it.unibs.pajc.redolfi.e08;

import javax.swing.SwingUtilities;

/** WhatsPAJC completa, il client: all'avvio il dialogo di connessione con la ricerca del server gia' partita. */
public class ClientApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatModel model = new ChatModel();
            ChatView view = new ChatView(model);
            ChatController controller = new ChatController(model, view);
            view.setVisible(true);
            DialogConnessione.chiedi(view, true).ifPresent(controller::connetti);
        });
    }
}
