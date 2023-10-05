package sockets;

import users.Transaction;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Listener {

    private static ArrayList<Socket> bootstrapNodes;
    private ArrayList<Socket> connectedNodes;
    private final ServerSocket serverSocket;

    public Listener(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        bootstrapNodes = new ArrayList<Socket>();
        connectedNodes = new ArrayList<Socket>();
        //bootstrapNodes.add(new Socket("localhost", 8008));
    }

    public void startListening() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(!serverSocket.isClosed()) {
                        Socket peerSocket = serverSocket.accept();
                        System.out.println("Peer connected from " + peerSocket.getInetAddress() + ":" + peerSocket.getPort());
                        handlePeer(peerSocket);
                    }
                } catch (IOException e) {
                    closeListener();
                }
            }
        }).start();

    }

    public void connectTo(String ip, int port) throws IOException {
        Socket peerSocket = new Socket(ip, port);
        System.out.println("Connected to peer at " + peerSocket.getInetAddress() + ":" + peerSocket.getPort());
        handlePeer(peerSocket);
    }

    private void handlePeer(Socket peerSocket) {
        PeerHandler peer = new PeerHandler(peerSocket);
        Thread peerThread = new Thread(peer);
        peerThread.start();
        connectedNodes.add(peerSocket);
    }

    public void sendMessage(String message) {
        PeerHandler.sendMessages(message);
    }

    public void sendTransaction(Transaction transaction) {
        PeerHandler.sendTransactions(transaction);
    }

    private void closeListener() {
        try {
            if(serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
