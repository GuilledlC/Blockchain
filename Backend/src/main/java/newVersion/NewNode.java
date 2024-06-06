package newVersion;

import database.Database;
import ledger.Block;
import users.Vote;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class NewNode {

	public NewNode() throws IOException {
		this.votes = new ArrayList<>();
		this.database = new Database("votesCheck");
		this.blocks = new ArrayList<>();

		this.userListener = new NewClientListener(8888);
		this.userListener.run();

		this.nodeListener = new NewNodeListener(9999);
		nodeListener.run();

		setNonMinedBlocks(bootstrapNodes);

	}

	private void syncVotes() {
		ArrayList<Vote> tempVotes = new ArrayList<>();
		tempVotes.addAll(NewClientHandler.getVotes());
		tempVotes.addAll(NewNodeHandler.getVotes());

		for (Vote v : tempVotes) {
			if(database.notExists(v.getKey().toString()) && Vote.verify(v)) { //Cambiar .getKey().toString()
				votes.add(v);
				database.changeValue(v.getKey().toString(), "1");
				NewNodeHandler.sendVoteToAll(v);
			}
		}
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
			nonminedblocks.add(new NonMinedBlock(socket.getInetAddress(), 1, 0));
	}

	private static void addEveryoneExcept(InetAddress ip){
		for (NonMinedBlock item : nonminedblocks){
			if (item.getNonMinedBlocks() != 0)
				item.setNonMinedBlocks(item.getNonMinedBlocks() + 1);
			if (item.getIp().equals(ip))
				item.setNonMinedBlocks(item.getNonMinedBlocks() - 1);
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
		for (NonMinedBlock item : nonminedblocks){
			if (item.getIp().equals(ip))
				item.setNonMinedBlocks(0);
		}
	}

	private int getNonMinedBlocksModule(){
		int aux = 0;
		for (NonMinedBlock item : nonminedblocks){
			aux += item.getNonMinedBlocks();
		}
		return aux;
	}

	private void resetNodes(){
		for (NonMinedBlock item : nonminedblocks)
			item.setMagicNumberCount(0);
	}

	private void proofOfConsensus(int magicNumber){
		int aux = -1;
		InetAddress miner = null;
		for (NonMinedBlock item : nonminedblocks){
			aux += item.getNonMinedBlocks();
			miner = item.getIp();
			if (aux >= magicNumber){
				actualminer = miner;
				break;
			}
		}
	}

	private void recieveActualMiner(){
		//Hacer que dure una cantidad de tiempo determinada
	}

	private InetAddress chooseActualMiner(){
		InetAddress ip = null;
		int aux = 0;
		for (NonMinedBlock item : nonminedblocks){
			if (aux < item.getMagicNumberCount()){
				ip = item.getIp();
				aux = item.getMagicNumberCount();
			}
		}
		return ip;
	}

	private boolean myTurnToMine(){
		return ip.equals(actualminer);
	}

	private void nodeExecution() {
		while (true) {
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
			proofOfConsensus(randomNumber);
			//todo send actualminer to nodes
			//todo receive actualminer from nodes recieveActualMiner();
			actualminer = chooseActualMiner();
			resetNodes();
			addEveryoneExcept(actualminer);
		}
	}

	private class NonMinedBlock {
		private final InetAddress ip;
		private int nonMinedBlocks;
		private int magicNumberCount;

		public NonMinedBlock(InetAddress ip, int nonMinedBlocks, int magicNumberCount) {
			this.ip = ip;
			this.nonMinedBlocks = nonMinedBlocks;
			this.magicNumberCount = magicNumberCount;
		}

		public InetAddress getIp() {
			return ip;
		}
		public int getNonMinedBlocks() {
			return nonMinedBlocks;
		}
		public void setNonMinedBlocks(int nonMinedBlocks) {
			this.nonMinedBlocks = nonMinedBlocks;
		}
		public int getMagicNumberCount() {
			return magicNumberCount;
		}
		public void setMagicNumberCount(int magicNumberCount) {
			this.magicNumberCount = magicNumberCount;
		}

	}

	private static ArrayList<Socket> bootstrapNodes;
	private final NewClientListener userListener;
	private final NewNodeListener nodeListener;
	static final int CHECK_VOTE_DELAY_S = 5;
	static final int BLOCK_BUILD_TIME_S = 5;
	static final int MIN_VOTES_BLOCK = 2;
	private static InetAddress ip = null;
	private static InetAddress actualminer = null;
	private static final ArrayList<NonMinedBlock> nonminedblocks = new ArrayList<>();
	private final ArrayList<Vote> votes;
	private final ArrayList<Block> blocks;
	Database database;


}
