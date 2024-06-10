package com.example.blockchain.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class NodeListener implements Runnable {

	private final ServerSocket listener;
	private static final ConcurrentHashMap<String, Socket> connections = new ConcurrentHashMap<>();

	public NodeListener(int port) throws IOException {
		this.listener = new ServerSocket(port);
	}

	public void remove(String id) {
		synchronized (connections) {
			connections.remove(id);
		}
	}

	public void connectTo(String ip) {
		try {
			String id = "/" + ip;
			synchronized (connections) {
				if (!connections.contains(id)) {
					SocketAddress sa = new InetSocketAddress(ip, 9999);
					Socket nodeSocket = new Socket();
					nodeSocket.connect(sa, 1000);
					connectTo(id, nodeSocket);
				} else {
					System.out.println("Ya estamos conectados con " + id);
				}
			}
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
		NodeHandler peer = new NodeHandler(nodeSocket, this);
		Thread peerThread = new Thread(peer);
		peerThread.start();
	}

}
