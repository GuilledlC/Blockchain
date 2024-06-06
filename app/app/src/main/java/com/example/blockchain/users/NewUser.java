package com.example.blockchain.users;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
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
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private static final ArrayList<Socket> bootstrapNodes = new ArrayList<>();

	public NewUser(PrivateKey privateKey, PublicKey publicKey) {
		this.priv = privateKey;
		this.pub = publicKey;
		//todo initialize bootstrap nodes
	}

	public void vote(String receiver) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
		byte[] signature = Vote.sign(receiver, priv);
		vote = new Vote(receiver, signature, pub);
		distributeVote();
	}

	private void distributeVote() {
		for(Socket node : bootstrapNodes) {
			sendToNode(node);
		}
	}

	private void sendToNode(Socket node) {
		if(node.isConnected()) {
			try {
				oos = new ObjectOutputStream(node.getOutputStream());
				ois = new ObjectInputStream(node.getInputStream());
				oos.writeObject(vote);
				node.close();
			} catch (IOException e) {
				System.out.println("Error sending vote");
				close();
			}
		}
	}

	private void close() {
		try {
			if (ois != null)
				ois.close();
			if (oos != null)
				oos.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
