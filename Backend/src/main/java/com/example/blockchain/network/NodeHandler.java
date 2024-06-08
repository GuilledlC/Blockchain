package com.example.blockchain.network;

import com.example.blockchain.ledger.Block;
import com.example.blockchain.ledger.Ledger;
import com.example.blockchain.users.Vote;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;

public class NodeHandler implements Runnable {

	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private static final ArrayList<NodeHandler> nodes = new ArrayList<>();
	private static final ArrayList<Vote> votes = new ArrayList<>();
	private static Block block;
	private static final ArrayList<InetAddress> chosenOnes = new ArrayList<>();
	private static final ArrayList<ArrayList<Block>> blockchainS = new ArrayList<>();

	public NodeHandler(Socket socket) {
		try {
			this.socket = socket;
			this.oos = new ObjectOutputStream(socket.getOutputStream());
			this.ois = new ObjectInputStream(socket.getInputStream());
			nodes.add(this);
			this.run();
		} catch (IOException e) {
			close();
		}
	}

	public static boolean isConnectedTo(InetAddress address) {
		for(NodeHandler nodeHandler : nodes) {
			if(nodeHandler.getIp().equals(address))
				return true;
		}
		return false;
	}

	@Override
	public void run() {
		while(socket.isConnected()) {
			try {
				Object object = ois.readObject();
				handleObjects(object);
			} catch (IOException | ClassNotFoundException e) {
				close();
				break;
			}
		}
	}

	private void handleObjects(Object object) {
		if (object instanceof Vote vote)
			votes.add(vote);
		else if(object instanceof Block block)
			this.block = new Block(block);
		else if(object instanceof InetAddress ip)
			chosenOnes.add(ip);
		else if (object instanceof String string) {
			if(string.equals("request"))
				sendBlockchain();
		} else if(object instanceof ArrayList<?> blockchain)
			blockchainS.add((ArrayList<Block>)blockchain);
	}

	public void sendObject(Serializable object) {
		try {
			oos.writeObject(object);
		} catch (IOException e) {
			close();
		}
	}

	private void sendBlockchain() {
		try {
			oos.writeObject(Ledger.getAllBlocks());
		} catch (IOException e) {
			close();
		}
	}

	private void close() {
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

	public InetAddress getIp() {
		return socket.getInetAddress();
	}

	public static ArrayList<Vote> getVotes() {
		ArrayList<Vote> returnVotes = new ArrayList<>(votes);
		votes.clear();
		return returnVotes;
	}
	public static void sendVoteToAll(Vote vote) {
		for(NodeHandler handler : nodes) {
			handler.sendObject(vote);
		}
	}

	public static Block getBlock() {
		Block returnBlock = new Block(block);
		block = null;
		return returnBlock;
	}
	public static void sendBlockToAll(Block block) {
		for(NodeHandler handler : nodes) {
			handler.sendObject(block);
		}
	}

	public static ArrayList<InetAddress> getChosenOnes() {
		ArrayList<InetAddress> returnChosenOnes = new ArrayList<>(chosenOnes);
		returnChosenOnes.sort(new Comparator<InetAddress>() {
			@Override
			public int compare(InetAddress o1, InetAddress o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		chosenOnes.clear();
		return returnChosenOnes;
	}
	public static void sendChosenOneToAll(InetAddress chosenOne) {
		for(NodeHandler handler : nodes)
			handler.sendObject(chosenOne);
	}

	public static ArrayList<ArrayList<Block>> getBlockchainS() {
		ArrayList<ArrayList<Block>> returnBlockchainS = new ArrayList<>(blockchainS);
		blockchainS.clear();
		return returnBlockchainS;
	}
	public static void requestBlockchainS() {
		for(NodeHandler handler : nodes)
			handler.sendObject("request");
	}

}
