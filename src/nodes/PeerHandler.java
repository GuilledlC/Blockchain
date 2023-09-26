package nodes;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class PeerHandler implements Runnable {
    private static ArrayList<PeerHandler> peerHandlers = new ArrayList<>();
    private Socket socket;
    protected BufferedReader bufferedReader;
    protected BufferedWriter bufferedWriter;
    private String id;

    public PeerHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.id = bufferedReader.readLine();
            peerHandlers.add(this);
        } catch (IOException e) {
            close();
        }
    }

    @Override
    public void run() {
        while(socket.isConnected()) {
            try {
                String message = bufferedReader.readLine();
                sendMessage(message);
            } catch (IOException e) {
                close();
                break;
            }
        }
    }

    private void sendMessage(String message) {
        for(PeerHandler peerHandler : peerHandlers) {
            try {
                if(!peerHandler.equals(this)) {
                    peerHandler.bufferedWriter.write(message);
                    peerHandler.bufferedWriter.newLine();
                    peerHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                close();
            }
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
