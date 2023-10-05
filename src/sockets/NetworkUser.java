package sockets;

import users.Transaction;

import java.io.IOException;

public class NetworkUser {
    private Listener listener = null;

    public void startListener(int port) throws IOException {
        listener = new Listener(port);
        listener.startListening();
    }

    public void connectTo(String address) throws IOException {
        String ip = address.substring(0, address.indexOf(':'));
        int port = Integer.parseInt(address.substring(address.indexOf(':') + 1));
        connectTo(ip, port);
    }

    private void connectTo(String ip, int port) throws IOException {
        listener.connectTo(ip, port);
    }

    public void sendMessage(String message) {
        if(isListening())
            listener.sendMessage(message);
    }

    public void sendTransaction(Transaction transaction) {
        if(isListening())
            listener.sendTransaction(transaction);
    }

    public boolean isListening() {
        return listener != null;
    }
}
