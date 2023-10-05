package sockets;

import users.Transaction;

import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;

public class PeerHandler implements Runnable {
    private static final ArrayList<PeerHandler> peerHandlers = new ArrayList<>();
    private Socket socket;
    protected ObjectInputStream ois;
    protected ObjectOutputStream oos;

    public PeerHandler(Socket socket) {
        try {
            this.socket = socket;
            this.ois = new ObjectInputStream(socket.getInputStream());
            this.oos = new ObjectOutputStream(socket.getOutputStream());
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
            } catch (IOException | ClassNotFoundException e) {
                close();
                break;
            }
        }
    }

    private void handleObjects(Object object) {
        if(object instanceof Transaction transaction) {
            try {
                System.out.println(Transaction.displayTransaction(transaction));
            } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        } else if (object instanceof String string) {
            System.out.println(string);
        }
    }

    protected static void sendObjects(Serializable object) {
        for(PeerHandler peerHandler : peerHandlers) {
            peerHandler.sendObject(object);
        }
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

}
