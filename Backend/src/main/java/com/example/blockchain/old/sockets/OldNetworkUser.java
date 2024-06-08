package com.example.blockchain.old.sockets;

import java.io.IOException;
import java.io.Serializable;

public class OldNetworkUser {

    protected final String id;
    protected OldListener oldListener = null;

    public OldNetworkUser(String id) {
        this.id = id;
    }

    public void startListener(int port) throws IOException {
        oldListener = new OldListener(port);
        oldListener.startListening();
    }

    public void connectTo(String address) throws IOException {
        String ip = address.substring(0, address.indexOf(':'));
        int port = Integer.parseInt(address.substring(address.indexOf(':') + 1));
        connectTo(ip, port);
    }

    private void connectTo(String ip, int port) throws IOException {
        oldListener.connectTo(ip, port);
    }

    public void sendObject(Serializable object) {
        if(isListening())
            oldListener.sendObject(object);
    }

    public boolean isListening() {
        return oldListener != null;
    }
}
