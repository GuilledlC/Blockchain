package com.example.blockchain.users;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

public class User implements Serializable {

	public User(PrivateKey privateKey, PublicKey publicKey) {
		this.priv = privateKey;
		this.pub = publicKey;

		initializeBootstrapNodes();
	}

	private void initializeBootstrapNodes() {
		bootstrapNodes.add("80.39.151.138");
		bootstrapNodes.add("86.127.225.89");
		bootstrapNodes.add("80.102.1.93");
	}

	public void vote(Vote vote) {
		this.vote = vote;
		distributeVote();
	}

	public Vote createVote(String receiver, PrivateKey priv, PublicKey pub) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
		byte[] signature = Vote.signVote(receiver, priv);
		Vote vote = new Vote(receiver, signature, pub);
		return vote;
	}

	public void vote(String receiver) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
		byte[] signature = Vote.signVote(receiver, priv);
		vote = new Vote(receiver, signature, pub);
		distributeVote();
	}

	private void distributeVote() {
		for(String ip : bootstrapNodes) {
			try {
				SocketAddress sa = new InetSocketAddress(ip, 8888);
				Socket socket = new Socket();
				socket.connect(sa, 20);
				sendToNode(socket);
			} catch (IOException e) {}
		}
	}

	private void sendToNode(Socket node) {
		if(node.isConnected()) {
			try {
				ObjectOutputStream oos = new ObjectOutputStream(node.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(node.getInputStream());
				oos.writeObject(vote);
				node.close();
				oos.close();
				ois.close();
			} catch (IOException e) {
				System.out.println("Error sending vote");
			}
		}
	}

	public boolean checkVote() throws NullPointerException {
		Stack<String> nodeStack = new Stack<>();
		nodeStack.addAll(bootstrapNodes);
		Collections.shuffle(nodeStack);

		while(!nodeStack.isEmpty()) {
			String ip = nodeStack.pop();
			try {
				SocketAddress sa = new InetSocketAddress(ip, 8888);
				Socket socket = new Socket();
				socket.connect(sa, 1000);
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				oos.writeObject(pub.getEncoded());
				Object object;
				while(socket.isConnected()) {
					try {
						object = ois.readObject();
						if(object instanceof Boolean bool)
							return bool;
					} catch (IOException | ClassNotFoundException ignored) {}
				}
				System.out.println("Socket is no longer connected");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		throw new NullPointerException();
	}


	private PrivateKey priv;
	private PublicKey pub;
	private Vote vote;
	private final ArrayList<String> bootstrapNodes = new ArrayList<>();

}
