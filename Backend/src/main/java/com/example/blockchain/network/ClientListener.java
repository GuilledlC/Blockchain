package com.example.blockchain.network;

import com.example.blockchain.nodes.Node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientListener implements Runnable {

	public ClientListener(int port, Node node) throws IOException {
		this.node = node;
		this.listener = new ServerSocket(port);
	}

	@Override
	public void run() {
		System.out.println("Client listener initiated");
		try {
			while(!listener.isClosed()) {
				Socket clientSocket = listener.accept();
				//System.out.println("Client connected from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
				handleClient(clientSocket);
			}
		} catch (IOException e) {

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
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	private Node node;
	private final ServerSocket listener;

}