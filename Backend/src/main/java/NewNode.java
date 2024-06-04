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

	public NewNode(int userPort, int nodePort) throws IOException {
		this.userListener =  new NewListener(new ServerSocket(userPort));
		this.nodeListener =  new NewListener(new ServerSocket(nodePort));
		this.votes = new ArrayList<>();
		this.blocks = new ArrayList<>();
		setNonMinedBlocks(bootstrapNodes);
	}

	private void syncVotes() {
		for (Vote v: NewPeerHandler.getVotes()) {
			if(!votes.contains(v))
				votes.add(v);
		}
	}

	protected ArrayList<Vote> getVotes() {
		syncVotes();
		return votes;
	}

	public ArrayList<Block> getBlocks() {
		return blocks;
	}

	protected void buildBlocks() {
		new Thread(() -> {
			while(votes.isEmpty()) {
				try {
					TimeUnit.SECONDS.sleep(CHECK_VOTE_DELAY_S);
				} catch (InterruptedException ignored) {}
				syncVotes();
			}

			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					while (votes.size() < MIN_VOTES_BLOCK) {
						try {
							TimeUnit.SECONDS.sleep(CHECK_VOTE_DELAY_S);
						} catch (InterruptedException ignored) {}
						syncVotes();
					}
					buildBlock();
				}
			}, BLOCK_BUILD_TIME_S * 1000, BLOCK_BUILD_TIME_S * 1000);
		}).start();
	}

	private void buildBlock() {
		syncVotes();
		Block block = new Block(votes);
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

	private boolean mining(){
		return ip.equals(actualminer);
	}

	private void nodeExecution(){
		Block minedblock;

		if (actualminer != null){
			if (mining()){
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
		int randomnumber = random.nextInt(getNonMinedBlocksModule());
		//todo send randomnumber
		int result = 0;//todo receive random numbers from nodes and change result
		proofOfConsensus(moduleOf(result, getNonMinedBlocksModule()));
		addEveryoneExcept(actualminer);
	}

	public void startListeners() {
		userListener.run();
		nodeListener.run();
	}

	private class NewListener implements Runnable {

		private final ServerSocket listener;
		private boolean isListening = false;
		private final ArrayList<NewPeerHandler> connectedPeers;

		public NewListener(ServerSocket listener) {
			this.listener = listener;
			this.connectedPeers = new ArrayList<>();
		}

		@Override
		public void run() {
			try {
				while(!listener.isClosed()) {
					isListening = true;
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
			connectedPeers.add(peer);
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
	private final NewListener userListener;
	private final NewListener nodeListener;
	static final int CHECK_VOTE_DELAY_S = 5;
	static final int BLOCK_BUILD_TIME_S = 5;
	static final int MIN_VOTES_BLOCK = 2;
	private static InetAddress ip = null;
	private static InetAddress actualminer = null;
	private static ArrayList<Object[]> nonminedblocks = new ArrayList<>();
	private final ArrayList<Vote> votes;
	private final ArrayList<Block> blocks;


}
