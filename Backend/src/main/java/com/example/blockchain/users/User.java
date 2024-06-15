package com.example.blockchain.users;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Random;

public class User implements Serializable {
	private PrivateKey priv;
	private PublicKey pub;
	private Vote vote;
	private final ArrayList<String> bootstrapNodes = new ArrayList<>();

	public User(PrivateKey privateKey, PublicKey publicKey) {
		this.priv = privateKey;
		this.pub = publicKey;

		//todo initialize bootstrap nodes
		initializeBootstrapNodes();
	}

	private void initializeBootstrapNodes() {
		bootstrapNodes.add("88.27.144.170");
		/*bootstrapNodes.add("80.39.151.138");
		bootstrapNodes.add("2.153.80.40");*/
	}

	public void vote(String receiver) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
		byte[] signature = Vote.sign(receiver, priv);
		vote = new Vote(receiver, signature, pub);
		distributeVote();
	}

	private void distributeVote() {
		for(String ip : bootstrapNodes) {
			try {
				SocketAddress sa = new InetSocketAddress(ip, 8888);
				Socket socket = new Socket();
				socket.connect(sa, 1000);
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
		Random random = new Random();
		int i = random.nextInt();
		i = Math.abs(i);
		i %= bootstrapNodes.size();
		String ip = bootstrapNodes.get(i);
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
		} catch (IOException e) {}

		throw new NullPointerException();
	}

}
