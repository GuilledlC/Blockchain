package com.example.blockchain.network;

import com.example.blockchain.ledger.Block;
import com.example.blockchain.ledger.Ledger;
import com.example.blockchain.users.Vote;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class NodeHandler implements Runnable {

	public NodeHandler(Socket socket, NodeListener listener) {
		this.listener = listener;
		try {
			this.socket = socket;
			this.oos = new ObjectOutputStream(socket.getOutputStream());
			this.ois = new ObjectInputStream(socket.getInputStream());
			System.out.println(ois == null);
			nodes.add(this);
		} catch (IOException e) {
			close();
		}
	}

	@Override
	public void run() {
		while(socket.isConnected()) {
			try {
				Object object = ois.readObject();
				handleObjects(object);
			} catch (IOException | ClassNotFoundException e) {
				System.out.println("close the nodehandler");
				close();
				break;
			}
		}
	}

	private void handleObjects(Object object) {
		if (object instanceof Vote vote) {
			votes.add(vote);
		}
		else if(object instanceof Block blocka) {
			block = new Block(blocka);
		}
		else if(object instanceof File blockFile) {
			Ledger.storeFile(blockFile);
		}
		else if (object instanceof String string) {
			if(string.equals("request")) {
				sendHash();
			}
			else if(string.equals("full")) {
				sendBlockchain();
			} else {
				if(!hasVotedMiner) {
					magicNumbers.add(string);
					System.out.println(this.getIp() + " has voted: " + string);
					hasVotedMiner = true;
				}
			}
		}
		else if(object instanceof byte[] hash) {
			hashes.putIfAbsent(hash, getIp());
		}
	}

	public void sendObject(Serializable object) {
		try {
			oos.writeObject(object);
		} catch (IOException e) {
			close();
		}
	}



	public static ArrayList<Vote> getVotes() {
		ArrayList<Vote> returnVotes = new ArrayList<>(votes);
		votes.clear();
		return returnVotes;
	}

	public static Block getBlock() {
		if(block == null)
			return null;
		Block returnBlock = new Block(block);
		block = null;
		return returnBlock;
	}
	public static void sendBlockToAll(Block block) {
		for(NodeHandler handler : nodes) {
			handler.sendObject(block);
			System.out.println("Bloque enviado");
		}
	}

	public static ArrayList<String> getMagicNumbers() {
		ArrayList<String> returnMagicNumbers = new ArrayList<>(magicNumbers);
		magicNumbers.clear();
		for(NodeHandler handler : nodes)
			handler.hasVotedMiner = false;
		return returnMagicNumbers;
	}
	public static void sendMagicNumberToAll(String magicNumber) {
		for(NodeHandler handler : nodes)
			handler.sendObject(magicNumber);
	}

	public static HashMap<byte[], String> getHashes() {
		HashMap<byte[], String> returnHashes = new HashMap<>(hashes);
		hashes.clear();
		return returnHashes;
	}
	public static void requestHashS() {
		for(NodeHandler handler : nodes)
			handler.sendObject("request");
	}
	private void sendHash() {
		try {
			oos.writeObject(Ledger.getLastBlock().getHash());
		} catch (IOException e) {
			close();
		}
	}
	public static void requestBlockchain(String ip) {
		for(NodeHandler handler : nodes) {
			if(handler.getIp().equals(ip))
				handler.sendObject("full");
		}
	}
	public void sendBlockchain() {
		try {
			for (int i = 0; i < Ledger.getSize(); i++) {
				oos.writeObject(Ledger.getFile(i));
			}
		} catch (IOException e) {
			close();
		}
	}

	private void close() {
		this.listener.remove(socket.getInetAddress().toString());
		try {
			if(ois != null)
				ois.close();
			if(oos != null)
				oos.close();
			if(socket != null)
				socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		nodes.remove(this);
	}

	public String getIp() {
		return socket.getInetAddress().toString();
	}


	NodeListener listener;
	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	public static final ArrayList<NodeHandler> nodes = new ArrayList<>();
	private static final ArrayList<Vote> votes = new ArrayList<>();
	private static Block block = null;
	private static final ArrayList<String> magicNumbers = new ArrayList<>();
	private static final HashMap<byte[], String> hashes = new HashMap<>();
	private boolean hasVotedMiner = false;

}
