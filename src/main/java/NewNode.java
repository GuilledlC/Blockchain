import sockets.PeerHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class NewNode {

	private final ArrayList<PeerHandler> connectedNodes;
	private static ArrayList<Socket> bootstrapNodes;
	private final ServerSocket userListener;
	private final ServerSocket nodeListener;

	public NewNode(int userPort, int nodePort) throws IOException {
		this.userListener = new ServerSocket(userPort);
		this.nodeListener = new ServerSocket(nodePort);
		this.connectedNodes = new ArrayList<>();
	}

	public void startListeners() {
		NewListener userThread = new NewListener(userListener);
		userThread.run();
		NewListener nodeThread = new NewListener(nodeListener);
		nodeThread.run();
	}

	private void handlePeer(Socket peerSocket) {
		PeerHandler peer = new PeerHandler(peerSocket);
		Thread peerThread = new Thread(peer);
		peerThread.start();
		connectedNodes.add(peer);
	}

	private void closeListener(ServerSocket listener) {
		try {
			listener.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
				closeListener(listener);
			}
		}
	}
}
