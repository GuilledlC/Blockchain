package sockets;

import users.Transaction;

import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;

public class PeerHandler implements Runnable {
    private static ArrayList<PeerHandler> peerHandlers = new ArrayList<>();
    private Socket socket;
    protected BufferedReader bufferedReader;
    protected BufferedWriter bufferedWriter;
    protected ObjectOutputStream oos;
    protected ObjectInputStream ois;
    private String id;

    public PeerHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.oos = new ObjectOutputStream(socket.getOutputStream());
            this.ois = new ObjectInputStream(socket.getInputStream());
            peerHandlers.add(this);
        } catch (IOException e) {
            close();
        }
    }

    @Override
    public void run() {
        //receiveMessages();
        receiveTransactions();
    }

    private void receiveMessages() {
        while(socket.isConnected()) {
            try {
                String message = bufferedReader.readLine();
                printMessage(message);
            } catch (IOException e) {
                close();
                break;
            }
        }
    }

    private void receiveTransactions() {
        while(socket.isConnected()) {
            try {
                Transaction transaction = (Transaction)ois.readObject();
                printMessage(Transaction.displayTransaction(transaction));
            } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
                close();
                break;
            }
        }
    }

    private void printMessage(String message) {
        System.out.println(message);
    }

    protected static void sendMessages(String message) {
        for(PeerHandler peerHandler : peerHandlers) {
            peerHandler.sendMessage(message);
        }
    }

    private void sendMessage(String message) {
        try {
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            close();
        }
    }

    protected static void sendTransactions(Transaction transaction) {
        for(PeerHandler peerHandler : peerHandlers) {
            peerHandler.sendTransaction(transaction);
        }
    }

    private void sendTransaction(Transaction transaction) {
        try {
            oos.writeObject(transaction);
        } catch (IOException e) {
            close();
        }
    }

    private void close() {
        peerHandlers.remove(this);
        try {
            if(bufferedReader != null)
                bufferedReader.close();
            if(bufferedWriter != null)
                bufferedWriter.close();
            if(socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
