package com.example.blockchain.sockets;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Listener {

	//Nodos que deberiamos tener hardcodeados
    private static ArrayList<Socket> bootstrapNodes;

	//Nodos que se conectan a nosotros
    private ArrayList<PeerHandler> connectedNodes;

	//El socket a traves del cual escuchamos
    private final ServerSocket serverSocket;

    public Listener(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        bootstrapNodes = new ArrayList<>();
        connectedNodes = new ArrayList<>();
    }

	//Un nuevo Thread a traves del cual recibimos conexiones y las inicializamos a sus propios Threads
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

	//Metodo para conectarse a una IP
    public void connectTo(String ip, int port) throws IOException {
        Socket peerSocket = new Socket(ip, port);
        System.out.println("Connected to peer at " + peerSocket.getInetAddress() + ":" + peerSocket.getPort());
        handlePeer(peerSocket);
    }

	//Metodo para crear un Thread con la conexion
    private void handlePeer(Socket peerSocket) {
        PeerHandler peer = new PeerHandler(peerSocket);
        Thread peerThread = new Thread(peer);
        peerThread.start();
        connectedNodes.add(peer);
    }

	//Metodo para enviar objetos a todos nuestros Peers
    public void sendObject(Serializable object) {
        PeerHandler.sendObjects(object);
    }

	//Metodo para recoger todos los objetos que nos han mandado nuestros Peers
    public ArrayList<Object> getObjects() {
        return PeerHandler.getObjects();
    }

	//Metodo para cerrar la conexion
    private void closeListener() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
