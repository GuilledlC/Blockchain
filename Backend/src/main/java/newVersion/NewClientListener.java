package newVersion;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NewClientListener implements Runnable {

	private final ServerSocket listener;
	private boolean isListening = false;

	public NewClientListener(int port) throws IOException {
		this.listener = new ServerSocket(port);
		this.run();
	}

	@Override
	public void run() {
		try {
			while(!listener.isClosed()) {
				isListening = true;
				Socket clientSocket = listener.accept();
				System.out.println("Client connected from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
				handleClient(clientSocket);
			}
		} catch (IOException e) {
			closeListener();
		}
	}

	private void handleClient(Socket peerSocket) {
		NewClientHandler peer = new NewClientHandler(peerSocket);
		Thread peerThread = new Thread(peer);
		peerThread.start();
	}

	private void closeListener() {
		try {
			listener.close();
			isListening = true;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isListening() {
		return isListening;
	}

}