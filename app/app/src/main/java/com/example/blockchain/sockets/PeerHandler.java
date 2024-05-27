package com.example.blockchain.sockets;



import com.example.blockchain.users.Vote;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class PeerHandler implements Runnable {
    private static final ArrayList<PeerHandler> peerHandlers = new ArrayList<>();
    private static final ArrayList<Object> objects = new ArrayList<>();
    private Socket socket;
    protected ObjectOutputStream oos;
    protected ObjectInputStream ois;

    public PeerHandler(Socket socket) {
        try {
            this.socket = socket;
            this.oos = new ObjectOutputStream(socket.getOutputStream());
            this.ois = new ObjectInputStream(socket.getInputStream());
            peerHandlers.add(this);
        } catch (IOException e) {
            close();
        }
    }

    @Override
    public void run() {
        receiveObjects();
    }


    private void receiveObjects() {
        while(socket.isConnected()) {
            try {
                Object object = ois.readObject();
                handleObjects(object);
                objects.add(object);
            } catch (IOException | ClassNotFoundException e) {
                close();
                break;
            }
        }
    }

    private void handleObjects(Object object) {
        if(object instanceof Vote vote)
            System.out.println("Received vote from " + socket.getInetAddress() + ": " + vote.displayVoteShort());
        else if (object instanceof String string)
            System.out.println(string);
    }

    protected static void sendObjects(Serializable object) {
        for(PeerHandler peerHandler : peerHandlers)
            peerHandler.sendObject(object);
    }

    private void sendObject(Serializable object) {
        try {
            oos.writeObject(object);
        } catch (IOException e) {
            close();
        }
    }

    private void close() {
        peerHandlers.remove(this);
        try {
            if(ois != null)
                ois.close();
            if(oos != null)
                oos.close();
            if(socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static ArrayList<Object> getObjects() {
        ArrayList<Object> aux = new ArrayList<>(objects);
        objects.clear();
        return aux;
    }

}
