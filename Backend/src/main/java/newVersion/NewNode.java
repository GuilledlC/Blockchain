package newVersion;

import ledger.Block;
import users.Vote;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class NewNode {

	public NewNode(int userPort) throws IOException {
		this.votes = new ArrayList<>();
		this.blocks = new ArrayList<>();
		this.userListener =  new NewClientListener(userPort);
		setNonMinedBlocks(bootstrapNodes);
	}

	private void syncVotes() {
		votes.addAll(NewClientHandler.getVotes());
	}

	private ArrayList<Vote> getVotes() {
		syncVotes();
		return votes;
	}

	public ArrayList<Block> getBlocks() {
		return blocks;
	}

	protected void buildBlocks() {
		new Thread(() -> {
			while(getVotes().isEmpty()) {
				try {
					TimeUnit.SECONDS.sleep(CHECK_VOTE_DELAY_S);
				} catch (InterruptedException ignored) {}
			}

			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					while (getVotes().size() < MIN_VOTES_BLOCK) {
						try {
							TimeUnit.SECONDS.sleep(CHECK_VOTE_DELAY_S);
						} catch (InterruptedException ignored) {}
					}
					buildBlock();
				}
			}, BLOCK_BUILD_TIME_S * 1000, BLOCK_BUILD_TIME_S * 1000);
		}).start();
	}

	private void buildBlock() {
		Block block = new Block(new ArrayList<>(getVotes()));
		blocks.add(block);
		votes.clear();
	}

	private Block recieveBlockFromMiner(InetAddress ip){
		//Not done yet
		Block block = null;
		return block;
	}

	private void setNonMinedBlocks(ArrayList<Socket> sockets) {
		for (Socket socket : sockets)
			nonminedblocks.add(new Object[]{socket.getInetAddress(), 1});
	}

	private static void addEveryoneExcept(InetAddress ip){
		for (Object[] item : nonminedblocks){
			if ((int)item[1] != 0) {
				item[1] = (double) item[1] + 1;
			}
			if (item[0].equals(ip)){
				item[1] = (double)item[1] - 1;
			}
		}
	}

	private boolean correctBlock (Block block) {
		//Not done yet
		boolean aux = true;
		while (aux) {
			//checkBlock
		}
		return aux;
	}

	private void punishNode(InetAddress ip){
		for (Object[] item : nonminedblocks){
			if (item[0].equals(ip)){
				item[1] = 0;
			}
		}
	}

	private int getNonMinedBlocksModule(){
		int aux = 0;
		for (Object[] item : nonminedblocks){
			aux += (int)item[1];
		}
		return aux;
	}

	private int moduleOf(int number, int module){
		return number % module;
	}

	private void proofOfConsensus(int magicNumber){
		int aux = -1;
		InetAddress miner = null;
		for (Object[] item : nonminedblocks){
			aux += (int)item[1];
			miner = (InetAddress)item[0];
			if (aux >= magicNumber){
				actualminer = miner;
				break;
			}
		}
	}

	private boolean myTurnToMine(){
		return ip.equals(actualminer);
	}

	private void nodeExecution() {
		Block minedblock;

		if (actualminer != null){
			if (myTurnToMine()){
				minedblock = new Block(getVotes());
				blocks.add(minedblock);
				//delete votes and update voted users
			}
			else{
				minedblock = recieveBlockFromMiner(actualminer);
				if (correctBlock(minedblock)){blocks.add(minedblock);}
				else {punishNode(actualminer);}
			}
		}

		Random random = new Random();
		int randomNumber = random.nextInt(getNonMinedBlocksModule());
		//todo send randomnumber
		int result = 0;//todo receive random numbers from nodes and change result
		proofOfConsensus(moduleOf(result, getNonMinedBlocksModule()));
		addEveryoneExcept(actualminer);
	}


	private class NewClientListener implements Runnable {

		private final ServerSocket listener;
		private boolean isListening = false;

		public NewClientListener(int port) throws IOException {
			this.listener = new ServerSocket(port);
			this.run();
		}

		@Override
		public void run() {
			try {
				while(!listener.isClosed()) {
					isListening = true;
					Socket clientSocket = listener.accept();
					System.out.println("Client connected from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
					handleClient(clientSocket);
				}
			} catch (IOException e) {
				closeListener();
			}
		}

		private void handleClient(Socket peerSocket) {
			NewClientHandler peer = new NewClientHandler(peerSocket);
			Thread peerThread = new Thread(peer);
			peerThread.start();
		}

		private void closeListener() {
			try {
				listener.close();
				isListening = true;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public boolean isListening() {
			return isListening;
		}

	}


	private static ArrayList<Socket> bootstrapNodes;
	private final NewClientListener userListener;
	static final int CHECK_VOTE_DELAY_S = 5;
	static final int BLOCK_BUILD_TIME_S = 5;
	static final int MIN_VOTES_BLOCK = 2;
	private static InetAddress ip = null;
	private static InetAddress actualminer = null;
	private static ArrayList<Object[]> nonminedblocks = new ArrayList<>();
	private final ArrayList<Vote> votes;
	private final ArrayList<Block> blocks;


}
