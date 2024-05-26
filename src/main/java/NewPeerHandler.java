import users.Vote;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class NewPeerHandler implements Runnable {

	private boolean isRunning;
	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private static final ArrayList<NewPeerHandler> newPeerHandlers = new ArrayList<>();
	public static final ArrayList<Vote> votes = new ArrayList<>();

	public static ArrayList<Vote> getVotes() {
		ArrayList<Vote> returnVotes = new ArrayList<>(votes);
		votes.clear();

		return returnVotes;
	}

	public NewPeerHandler(Socket socket) {
		try {
			this.socket = socket;
			this.oos = new ObjectOutputStream(socket.getOutputStream());
			this.ois = new ObjectInputStream(socket.getInputStream());
			isRunning = true;
			newPeerHandlers.add(this);
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
			/*try {
				oos.writeObject("Vote received");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}*/
		}
	}

	private void close() {
		newPeerHandlers.remove(this);
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
		isRunning = false;
	}

	public boolean isRunning() {
		return isRunning;
	}
}
