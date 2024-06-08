package com.example.blockchain.users;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.ArrayList;

public class NewUser implements Serializable {
	private PrivateKey priv;
	private PublicKey pub;
	private Vote vote;
	private final ArrayList<String> bootstrapNodes = new ArrayList<>();

	public NewUser(PrivateKey privateKey, PublicKey publicKey) {
		this.priv = privateKey;
		this.pub = publicKey;

		//todo initialize bootstrap nodes
		initializeBootstrapNodes();
	}

	private void initializeBootstrapNodes() {
		bootstrapNodes.add("10.0.2.2");
	}

	public void vote(String receiver) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
		byte[] signature = Vote.sign(receiver, priv);
		vote = new Vote(receiver, signature, pub);
		distributeVote();
	}

	private void distributeVote() {
		for(String ip : bootstrapNodes) {
			try {
				sendToNode(new Socket(ip, 8888));
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

}
