package nodes;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Listener {

    private static ArrayList<Socket> bootstrapNodes;
    private ArrayList<Socket> connectedNodes;
    private ServerSocket serverSocket;

    public Listener(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        bootstrapNodes = new ArrayList<Socket>();
        //bootstrapNodes.add(new Socket("localhost", 8008));
    }

    public void startListening() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(!serverSocket.isClosed()) {
                        Socket peerSocket = serverSocket.accept();
                        System.out.println("Peer connected from " + peerSocket.getInetAddress());

                        PeerHandler peer = new PeerHandler(peerSocket);
                        Thread peerThread = new Thread(peer);
                        peerThread.start();
                    }
                } catch (IOException e) {
                    closeListener();
                }
            }
        }).start();

    }

    public void connectTo(String ip, int port) throws IOException {
        Socket socket = new Socket(ip, port);
        System.out.println("Connected to peer at " + socket.getInetAddress());
        PeerHandler peer = new PeerHandler(socket);
        Thread peerThread = new Thread(peer);
        peerThread.start();
    }

    protected void sendMessage(String message) {
        PeerHandler.sendMessages(message);
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
