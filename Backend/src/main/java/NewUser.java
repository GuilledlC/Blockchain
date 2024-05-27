import users.Vote;
import utils.HashUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;

public class NewUser {
	private PrivateKey priv;
	private PublicKey pub;
	private byte[] address;
	private Vote vote;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private static final ArrayList<Socket> bootstrapNodes = new ArrayList<>();

	public NewUser() {
		getKeys();
	}

	private void getKeys() {
		//Get private key
		//Get public key
		address = HashUtils.hashString(pub.toString());
	}

	protected void vote(String receiver) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
		String voteString = HashUtils.toHexString(address) + " " + receiver;
		byte[] signature = Vote.sign(voteString, priv);
		vote = new Vote(address, voteString, signature, pub);
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
