package newVersion;

import java.io.IOException;
import java.net.ServerSocket;

public class NewNodeListener implements Runnable {

	private final ServerSocket listener;
	private boolean isListening = false;

	public NewNodeListener(int port) throws IOException {
		this.listener = new ServerSocket(port);
		this.run();
	}

	@Override
	public void run() {

	}
}
