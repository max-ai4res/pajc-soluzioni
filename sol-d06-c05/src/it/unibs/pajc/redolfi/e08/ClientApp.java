package it.unibs.pajc.redolfi.e08;

import javax.swing.SwingUtilities;

/** WhatsPAJC con le conversazioni separate: il main. Server: sol-d06-c07 (o il capitolo 4). */
public class ClientApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatModel model = new ChatModel();
            ChatView view = new ChatView(model);
            new ChatController(model, view);
            view.setVisible(true);
        });
    }
}
