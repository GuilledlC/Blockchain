package com.example.blockchain.network;

import com.example.blockchain.nodes.Node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientListener implements Runnable {

	private Node node;
	private final ServerSocket listener;
	private boolean isListening = false;

	public ClientListener(int port, Node node) throws IOException {
		this.node = node;
		this.listener = new ServerSocket(port);
	}

	@Override
	public void run() {
		System.out.println("Client listener initiated");
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
		ClientHandler peer = new ClientHandler(peerSocket, node);
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