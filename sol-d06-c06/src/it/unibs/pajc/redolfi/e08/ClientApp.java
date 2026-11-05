package it.unibs.pajc.redolfi.e08;

import javax.swing.SwingUtilities;

/**
 * Esercizio 6.8 - WhatsPAJC con la connessione automatica: all'avvio apre il dialogo con
 * la ricerca gia' partita (Scoperta.cerca in un SwingWorker); se il server risponde,
 * host e porta sono gia' compilati e resta da scrivere il nome. Il resto e' il client
 * con la finestra: la Connessione consegna le righe del server sull'EDT e il Controller
 * le interpreta con la tabella delle risposte, speculare a quella dei comandi del server;
 * le righe dell'utente partono come sono. Server: sol-d06-c07.
 */
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
