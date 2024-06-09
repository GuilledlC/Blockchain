package com.example.blockchain.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class NodeListener implements Runnable {

	private final ServerSocket listener;
	public final ConcurrentHashMap<String, Socket> connections = new ConcurrentHashMap<>();

	public NodeListener(int port) throws IOException {
		this.listener = new ServerSocket(port);
	}


	public void connectTo(String ip) {
		try {
			String id = "/" + ip;
			Socket nodeSocket = new Socket(ip, 9999);
			connectTo(id, nodeSocket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		System.out.println("Node listener initiated");
		while(!listener.isClosed()) {
			try {
				Socket nodeSocket = listener.accept();
				String id = nodeSocket.getInetAddress().toString();
				connectTo(id, nodeSocket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void connectTo(String id, Socket nodeSocket) {
		try {
			synchronized (connections) {
				if(connections.putIfAbsent(id, nodeSocket) == null) {
					handleNode(nodeSocket);
					System.out.println("Conectado con " + id);
				} else {
					System.out.println("Ya estamos conectados con " + id);
					nodeSocket.close();
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
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
