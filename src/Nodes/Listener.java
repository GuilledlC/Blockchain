package Nodes;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Listener {

    private static ArrayList<Socket> BootstrapNodes;
    private ArrayList<Socket> connectedNodes;
    private ServerSocket serverSocket;

    public Listener(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);

        BootstrapNodes.add(new Socket("localhost", 8008));
    }

    public void startListening() {
        try {
            while(!serverSocket.isClosed()) {
                Socket peerSocket = serverSocket.accept();
                System.out.println("Connected to peer at " + peerSocket.getInetAddress());

                PeerHandler peer = new PeerHandler(peerSocket);
                Thread peerThread = new Thread(peer);
                peerThread.start();
            }
        } catch (IOException e) {
            closeListener();
        }
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
