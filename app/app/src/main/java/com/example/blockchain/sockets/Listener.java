package com.example.blockchain.sockets;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Listener {

    private static ArrayList<Socket> bootstrapNodes;
    private ArrayList<PeerHandler> connectedNodes;
    private final ServerSocket serverSocket;

    public Listener(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        bootstrapNodes = new ArrayList<>();
        connectedNodes = new ArrayList<>();
        //bootstrapNodes.add(new Socket("localhost", 8008));
    }

    public void startListening() {
        new Thread(() -> {
            try {
                while(!serverSocket.isClosed()) {
                    Socket peerSocket = serverSocket.accept();
                    System.out.println("Peer connected from " + peerSocket.getInetAddress() + ":" + peerSocket.getPort());
                    handlePeer(peerSocket);
                }
            } catch (IOException e) {
                closeListener();
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
        connectedNodes.add(peer);
    }

    public void sendObject(Serializable object) {
        PeerHandler.sendObjects(object);
    }

    public ArrayList<Object> getObjects() {
        return PeerHandler.getObjects();
    }

    private void closeListener() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
