package newVersion;

import ledger.Block;
import users.Vote;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class NewNodeHandler implements Runnable {

	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private static final ArrayList<Vote> votes = new ArrayList<>();
	private static final ArrayList<Block> blocks = new ArrayList<>();
	private static final ArrayList<Integer> randomNumbers = new ArrayList<>();
	private static final ArrayList<NewNodeHandler> nodes = new ArrayList<>();


	public static ArrayList<Vote> getVotes() {
		ArrayList<Vote> returnVotes = new ArrayList<>(votes);
		votes.clear();
		return returnVotes;
	}

	public static ArrayList<Block> getBlocks() {
		ArrayList<Block> returnBlocks = new ArrayList<>(blocks);
		blocks.clear();
		return returnBlocks;
	}

	public static ArrayList<Integer> getRandomNumbers() {
		ArrayList<Integer> returnRandomNumbers = new ArrayList<>(randomNumbers);
		randomNumbers.clear();
		return returnRandomNumbers;
	}

	public static void sendVoteToAll(Vote vote) {
		for(NewNodeHandler handler : nodes) {
			handler.sendVote(vote);
		}
	}

	public NewNodeHandler(Socket socket) {
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
			blocks.add(block);
		else if(object instanceof Integer integer)
			randomNumbers.add(integer);
	}

	public void sendVote(Vote vote) {
		try {
			oos.writeObject(vote);
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
	}

	public InetAddress getIp() {
		return socket.getInetAddress();
	}
}
