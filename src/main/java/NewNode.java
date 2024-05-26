import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class NewNode {

	private final ArrayList<NewPeerHandler> connectedNodes;
	private static ArrayList<Socket> bootstrapNodes;
	private final NewListener userListener;
	private final NewListener nodeListener;

	public NewNode(int userPort, int nodePort) throws IOException {
		this.userListener =  new NewListener(new ServerSocket(userPort));
		this.nodeListener =  new NewListener(new ServerSocket(nodePort));
		this.connectedNodes = new ArrayList<>();
	}

	public void startListeners() {
		userListener.run();
		nodeListener.run();
	}

	private class NewListener implements Runnable {

		private final ServerSocket listener;

		public NewListener(ServerSocket listener) {
			this.listener = listener;
		}

		@Override
		public void run() {
			try {
				while(!listener.isClosed()) {
					Socket peerSocket = listener.accept();
					System.out.println("Peer connected from " + peerSocket.getInetAddress() + ":" + peerSocket.getPort());
					handlePeer(peerSocket);
				}
			} catch (IOException e) {
				closeListener();
			}
		}

		private void handlePeer(Socket peerSocket) {
			NewPeerHandler peer = new NewPeerHandler(peerSocket);
			Thread peerThread = new Thread(peer);
			peerThread.start();
			connectedNodes.add(peer);
		}

		private void closeListener() {
			try {
				listener.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
