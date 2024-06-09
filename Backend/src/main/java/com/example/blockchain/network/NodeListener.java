package com.example.blockchain.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class NodeListener implements Runnable {

	private final ServerSocket listener;
	private final ConcurrentHashMap<String, Socket> connections = new ConcurrentHashMap<>();

	public void connectTo(String ip) {
		synchronized (connections) {
			if(connections.contains(ip)) {
				System.out.println("Ya estamos conectados con " + ip + "!");
				return;
			}
		}

		try {
			Socket nodeSocket = new Socket(ip, 9999);
			synchronized (connections) {
				connections.put(ip, nodeSocket);
			}
			System.out.println("Conectado con " + ip + "!");
		} catch (IOException e) {
			System.out.println("Error al conectar con el nodo " + ip + ": " + e.getMessage());
		}
	}

	public NodeListener(int port) throws IOException {
		this.listener = new ServerSocket(port);
	}

	@Override
	public void run() {
		System.out.println("Node listener initiated");
		try {
			while(!listener.isClosed()) {
				Socket nodeSocket = listener.accept();
				String id = nodeSocket.getInetAddress().toString();
				if(connections.putIfAbsent(id, nodeSocket) == null) {
					System.out.println("Conectado con " + id);
					handleNode(nodeSocket);
				} else {
					System.out.println("Ya estamos conectados con " + id);
					nodeSocket.close();
				}
			}
		} catch (IOException e) {
			closeListener();
		}
	}

	private void closeListener() {
		try {
			listener.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void handleNode(Socket nodeSocket) {
		NodeHandler peer = new NodeHandler(nodeSocket);
		Thread peerThread = new Thread(peer);
		peerThread.start();
	}

}
