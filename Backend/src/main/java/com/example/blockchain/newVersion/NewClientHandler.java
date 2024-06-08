package com.example.blockchain.newVersion;

import com.example.blockchain.users.Vote;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class NewClientHandler implements Runnable {

	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private static final ArrayList<Vote> votes = new ArrayList<>();


	public static ArrayList<Vote> getVotes() {
		ArrayList<Vote> returnVotes = new ArrayList<>(votes);
		votes.clear();
		return returnVotes;
	}


	public NewClientHandler(Socket socket) {
		try {
			this.socket = socket;
			this.oos = new ObjectOutputStream(socket.getOutputStream());
			this.ois = new ObjectInputStream(socket.getInputStream());
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
		if (object instanceof Vote vote) {
			votes.add(vote);
			System.out.println("Vote received");
		}
		close();
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
