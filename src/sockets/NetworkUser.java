package sockets;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class NetworkUser {

    protected final String id;
    private Listener listener = null;

    public NetworkUser(String id) {
        this.id = id;
    }

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

    public void sendObject(Serializable object) {
        if(isListening())
            listener.sendObject(object);
    }

    public ArrayList<Object> getObjects() {
        return listener.getObjects();
    }

    public boolean isListening() {
        return listener != null;
    }
}
