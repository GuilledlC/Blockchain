package newVersion;

import database.Database;
import ledger.Block;
import users.Vote;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

public class NewNode {

	public NewNode() throws IOException, InterruptedException  {
		this.votes = new ArrayList<>();
		this.database = new Database("votesCheck");

		this.blocks = new ArrayList<>();
		chooseBlockchain();

		this.userListener = new NewClientListener(8888);
		this.userListener.run();
		this.nodeListener = new NewNodeListener(9999);
		this.nodeListener.run();

		setNonMinedBlocks(bootstrapNodes);

		nodeExecution();
	}

	private void chooseBlockchain() {
		ArrayList<Block> chosenBlockchain = new ArrayList<>();
		NewNodeHandler.requestBlockchainS();
		ArrayList<ArrayList<Block>> blockchainS = NewNodeHandler.getBlockchainS();
		HashMap<byte[], Integer> election = new HashMap<>();

		//Por cada una de las listas de bloques que tengo (que asumo que estan ordenadas)
		for (ArrayList<Block> blockchain : blockchainS) {
			//Cojo el ultimo bloque
			Block aux = blockchain.get(blockchain.size() - 1);
			//Si no existe lo apunto
			if(!election.containsKey(aux.getHash()))
				election.put(aux.getHash(), 0);
			//Le voto
			election.put(aux.getHash(), election.get(aux.getHash()) + 1);
		}

		int max = 0;
		for(ArrayList<Block> blockchain : blockchainS) {
			Block aux = blockchain.get(blockchain.size() - 1);
			int num = election.get(aux.getHash());
			if(num > max) {
				max = num;
				chosenBlockchain = blockchain;
			}
		}

		this.blocks.addAll(chosenBlockchain);
	}

	private void syncVotes() {
		ArrayList<Vote> tempVotes = new ArrayList<>();
		tempVotes.addAll(NewClientHandler.getVotes());
		tempVotes.addAll(NewNodeHandler.getVotes());

		for (Vote v : tempVotes) {
			if(database.notExists(v.getKey().getEncoded()) && Vote.verify(v)) { //Cambiar .getKey().toString()
				votes.add(v);
				database.putValue(v.getKey().getEncoded(), Database.State.InPool);
				NewNodeHandler.sendVoteToAll(v);
			}
		}
	}

	private void syncChosenOnes() {
		ArrayList<InetAddress> tempChosenOnes = NewNodeHandler.getChosenOnes();
		int count = 0;
		for(NonMinedBlock item : nonMinedBlocks) {
			while(count < tempChosenOnes.size() && tempChosenOnes.get(count).equals(item.getIp())) {
				item.setMagicNumberCount(item.getMagicNumberCount() + 1);
				count++;
			}
		}
	}

	private void syncBlock() {
		Block block = NewNodeHandler.getBlock();
		if (correctBlock(block)) {
			blocks.add(block);
			for(Vote v : block.getVotes()) {
				votes.remove(v);
				database.putValue(v.getKey().getEncoded(), Database.State.Voted);
			}
		}
		else
			punishNode(actualMiner);
	}

	public ArrayList<Block> getBlocks() {
		return blocks;
	}

	private void setNonMinedBlocks(ArrayList<Socket> sockets) {
		for (Socket socket : sockets)
			nonMinedBlocks.add(new NonMinedBlock(socket.getInetAddress(), 1, 0));
		nonMinedBlocks.sort(new Comparator<NonMinedBlock>() {
			@Override
			public int compare(NonMinedBlock o1, NonMinedBlock o2) {
				return o1.getIp().toString().compareTo(o2.getIp().toString());
			}
		});
	}

	private void addEveryoneExcept(InetAddress ip){
		for (NonMinedBlock item : nonMinedBlocks) {
			if (item.getNonMinedBlocks() != 0)
				item.setNonMinedBlocks(item.getNonMinedBlocks() + 1);
			if (item.getIp().equals(ip))
				item.setNonMinedBlocks(item.getNonMinedBlocks() - 1);
		}
	}

	private boolean correctBlock (Block block) {
		/**La comprobacion se basará en comprobar si el hash del bloque anterior
		 * es igual que el del bloque anterior de la blockchain del nodo,
		 * si cada voto pertenece a un votante que no ha votado aun
		 * y comprobar que los votos sean correctos (que la firma funcione con la publica).**/
		ArrayList<Vote> tempVotes = block.getVotes();
		boolean aux = Arrays.equals(block.getPreviousHash(), getLastBlock().getHash());
		int count = 0;
		while (aux && count < tempVotes.size()) {
			Vote tempVote = tempVotes.get(count);
			aux = !database.hasVoted(tempVote.getKey().getEncoded()) && Vote.verify(tempVote);
			count++;
		}
		return aux;
	}

	private Block getLastBlock() {
		return blocks.get(blocks.size() - 1);
	}

	private void punishNode(InetAddress ip) {
		for (NonMinedBlock item : nonMinedBlocks){
			if (item.getIp().equals(ip))
				item.setNonMinedBlocks(0);
		}
	}

	private int getNonMinedBlocksModule() {
		int aux = 0;
		for (NonMinedBlock item : nonMinedBlocks){
			aux += item.getNonMinedBlocks();
		}
		return aux;
	}

	private void resetNodes() {
		for (NonMinedBlock item : nonMinedBlocks)
			item.setMagicNumberCount(0);
	}

	private InetAddress proofOfConsensus(int magicNumber) {
		int aux = -1;
		InetAddress miner = null;
		for (NonMinedBlock item : nonMinedBlocks){
			aux += item.getNonMinedBlocks();
			miner = item.getIp();
			if (aux >= magicNumber){
				return miner;
			}
		}
		return null;
	}

	private InetAddress chooseActualMiner() {
		InetAddress ip = null;
		int aux = 0;
		for (NonMinedBlock item : nonMinedBlocks){
			if (aux < item.getMagicNumberCount()){
				ip = item.getIp();
				aux = item.getMagicNumberCount();
			}
		}
		return ip;
	}

	private boolean myTurnToMine(){
		return ip.equals(actualMiner);
	}

	//todo lidiar con la excepcion
	private void nodeExecution() throws InterruptedException {
		Thread mineThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Block minedblock;
						if (actualMiner != null) {
							if (myTurnToMine()){
								syncVotes();
								minedblock = new Block(votes, getLastBlock());

								//Add block to ledger
								blocks.add(minedblock);

								//Delete votes and update voted users
								for(Vote v : minedblock.getVotes()) {
									database.putValue(v.getKey().getEncoded(), Database.State.Voted);
								}
								votes.clear();

								//Send block to everyone
								NewNodeHandler.sendBlockToAll(minedblock);

							}
							else
								syncBlock();

							actualMiner = null;

						} else
							Thread.sleep(5000);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}
		});
		mineThread.start();

		Thread minerElection = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Random random = new Random();
						int randomNumber = random.nextInt(getNonMinedBlocksModule());
						InetAddress chosenMiner = proofOfConsensus(randomNumber);
						NewNodeHandler.sendChosenOneToAll(chosenMiner); //Send chosen miner to nodes
						Thread.sleep(60000); //60s
						syncChosenOnes(); //Receive actualMiner from nodes receiveActualMiner();
						actualMiner = chooseActualMiner();
						resetNodes();
						addEveryoneExcept(actualMiner);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}
		});
		minerElection.start();
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
	private static InetAddress actualMiner = null;
	private final ArrayList<NonMinedBlock> nonMinedBlocks = new ArrayList<>();
	private final ArrayList<Vote> votes;
	private final ArrayList<Block> blocks;
	Database database;


}
