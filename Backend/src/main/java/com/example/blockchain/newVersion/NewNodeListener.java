package com.example.blockchain.newVersion;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NewNodeListener implements Runnable {

	private final ServerSocket listener;
	private boolean isListening = false;

	public NewNodeListener(int port) throws IOException {
		this.listener = new ServerSocket(port);
	}

	@Override
	public void run() {
		System.out.println("Node listener initiated");
		try {
			while(!listener.isClosed()) {
				isListening = true;
				Socket clientSocket = listener.accept();
				System.out.println("Client connected from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
				handleNode(clientSocket);
			}
		} catch (IOException e) {
			closeListener();
		}
	}

	private void closeListener() {
		try {
			listener.close();
			isListening = true;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void handleNode(Socket peerSocket) {
		NewNodeHandler peer = new NewNodeHandler(peerSocket);
		Thread peerThread = new Thread(peer);
		peerThread.start();
	}

	public boolean isListening() {
		return isListening;
	}

}
