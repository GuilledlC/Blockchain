import users.Vote;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class NewPeerHandler implements Runnable {

	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private static final ArrayList<NewPeerHandler> peerHandlers = new ArrayList<>();
	private static final ArrayList<Vote> votes = new ArrayList<>();

	public NewPeerHandler(Socket socket) {
		try {
			this.socket = socket;
			this.oos = new ObjectOutputStream(socket.getOutputStream());
			this.ois = new ObjectInputStream(socket.getInputStream());
			peerHandlers.add(this);
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
		}
	}

	private void close() {
		peerHandlers.remove(this);
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
}
