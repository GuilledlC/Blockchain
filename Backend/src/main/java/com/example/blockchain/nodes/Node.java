package com.example.blockchain.nodes;

import com.example.blockchain.database.Database;
import com.example.blockchain.ledger.Block;
import com.example.blockchain.ledger.Ledger;
import com.example.blockchain.network.ClientHandler;
import com.example.blockchain.network.ClientListener;
import com.example.blockchain.network.NodeHandler;
import com.example.blockchain.network.NodeListener;
import com.example.blockchain.users.Vote;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class Node {

	private String ip = "88.27.144.170";

	public Node() throws IOException, InterruptedException  {
		this.votes = new ArrayList<>();
		this.database = new Database("votesCheck");
		this.blocks = new ArrayList<>();

		this.userListener = new ClientListener(8888);
		Thread userThread = new Thread(this.userListener);
		userThread.start();

		this.nodeListener = new NodeListener(9999);
		Thread nodeThread = new Thread(this.nodeListener);
		nodeThread.start();

		//todo meter en un thread
		initializeBootstrapNodes();
		connectToBootstrapNodes();
		setNonMinedBlocks(bootstrapNodes);

		//chooseBlockchain();
		Ledger.dropBlocks();
		this.blocks.add(Block.getGenesis());
		Ledger.storeBlock(Block.getGenesis());
		//nodeExecution();
	}

	private void connectToBootstrapNodes() {
		for(String address : bootstrapNodes) {
			nodeListener.connectTo(address);
		}
	}

	private void initializeBootstrapNodes() {
		bootstrapNodes.add("80.39.151.138");
		bootstrapNodes.add("2.153.80.40");
		bootstrapNodes.add("88.27.144.170");

		bootstrapNodes.remove(ip);
	}

	private void chooseBlockchain() {
		Ledger.dropBlocks();

		ArrayList<Block> chosenBlockchain = new ArrayList<>();
		NodeHandler.requestBlockchainS();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		ArrayList<ArrayList<Block>> blockchainS = NodeHandler.getBlockchainS();
		HashMap<byte[], Integer> election = new HashMap<>();

		//Por cada una de las listas de bloques que tengo (que asumo que estan ordenadas)
		for (ArrayList<Block> blockchain : blockchainS) {
			if(blockchain.size() == 0)
				continue;
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
			if(blockchain.size() == 0)
				continue;
			Block aux = blockchain.get(blockchain.size() - 1);
			int num = election.get(aux.getHash());
			if(num > max) {
				max = num;
				chosenBlockchain = blockchain;
			}
		}

		if(max == 0)
			storeBlock(Block.getGenesis());
		else
			storeBlocks(chosenBlockchain);
		System.out.println("Blockchain chosen");
	}

	private void syncVotes() throws IOException, InvalidKeySpecException {
		ArrayList<Vote> tempVotes = new ArrayList<>();
		tempVotes.addAll(ClientHandler.getVotes());
		tempVotes.addAll(NodeHandler.getVotes());

		for (Vote v : tempVotes) {
			if(database.notExists(v.getKey()) && Vote.verify(v)) { //Cambiar .getKey().toString()
				votes.add(v);
				database.putValue(v.getKey(), Database.State.InPool);
				NodeHandler.sendVoteToAll(v);
			}
		}
	}

	private void syncChosenOnes() {
		ArrayList<String> tempChosenOnes = NodeHandler.getChosenOnes();
		int count = 0;
		for(NonMinedBlock item : nonMinedBlocks) {
			while(count < tempChosenOnes.size() && tempChosenOnes.get(count).equals(item.getIp())) {
				item.setMagicNumberCount(item.getMagicNumberCount() + 1);
				count++;
			}
		}
	}

	private void syncBlock() throws IOException, InvalidKeySpecException {
		//todo esperar 10s a que lo mande el nodo minero
		Block block = NodeHandler.getBlock();
		while (block == null) {
			try {
				Thread.sleep(5000); //todo calibrar tiempo
				System.out.println("esperando a un bloque");
				block = NodeHandler.getBlock();
			} catch (InterruptedException e) {throw new RuntimeException(e);}
		}

		if (correctBlock(block)) {
			storeBlock(block);
			for(Vote v : block.getVotes()) {
				votes.remove(v);
				database.putValue(v.getKey(), Database.State.Voted);
			}
		} else
			punishNode(actualMiner);
	}

	public ArrayList<Block> getBlocks() {
		return blocks;
	}

	private void setNonMinedBlocks(ArrayList<String> sockets) {
		for (String ip : sockets)
			this.nonMinedBlocks.add(new NonMinedBlock(ip, 1, 0));

		this.nonMinedBlocks.add(new NonMinedBlock(this.ip, 1, 0));
		this.nonMinedBlocks.sort(new Comparator<NonMinedBlock>() {
			@Override
			public int compare(NonMinedBlock o1, NonMinedBlock o2) {
				return o1.getIp().compareTo(o2.getIp());
			}
		});

		System.out.println(nonMinedBlocks);
	}

	private void addEveryoneExcept(String ip){
		for (NonMinedBlock item : nonMinedBlocks) {
			if (item.getNonMinedBlocks() != 0)
				item.setNonMinedBlocks(item.getNonMinedBlocks() + 1);
			if (item.getIp().equals(ip))
				item.setNonMinedBlocks(item.getNonMinedBlocks() - 1);
		}
	}

	private boolean correctBlock (Block block) throws IOException, InvalidKeySpecException {
		/**La comprobacion se basará en comprobar si el hash del bloque anterior
		 * es igual que el del bloque anterior de la blockchain del nodo,
		 * si cada voto pertenece a un votante que no ha votado aun
		 * y comprobar que los votos sean correctos (que la firma funcione con la publica).**/
		ArrayList<Vote> tempVotes = block.getVotes();
		boolean aux = Arrays.equals(block.getPreviousHash(), getLastBlock().getHash());
		int count = 0;
		while (aux && count < tempVotes.size()) {
			Vote tempVote = tempVotes.get(count);
			aux = !database.hasVoted(tempVote.getKey()) && Vote.verify(tempVote);
			count++;
		}
		return aux;
	}

	private Block getLastBlock() {
		return blocks.get(blocks.size() - 1);
	}

	private void storeBlocks(ArrayList<Block> blocks) {
		this.blocks.addAll(blocks);
		Ledger.storeBlocks(blocks);
	}

	private void storeBlock(Block block) {
		this.blocks.add(block);
		Ledger.storeBlock(block);
	}

	private void punishNode(String ip) {
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

	private String proofOfConsensus(int magicNumber) {
		int aux = -1;
		String miner = null;
		for (NonMinedBlock item : nonMinedBlocks){
			aux += item.getNonMinedBlocks();
			miner = item.getIp();
			if (aux >= magicNumber){
				return miner;
			}
		}
		return null;
	}

	private String chooseActualMiner() {
		String ip = null;
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

	private void nodeExecution() throws InterruptedException {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						System.out.println("choosing miner");
						Random random = new Random();
						int randomNumber = random.nextInt(0, getNonMinedBlocksModule());
						chosenMiner = proofOfConsensus(randomNumber);
						NodeHandler.sendChosenOneToAll(chosenMiner); //Send chosen miner to nodes
						System.out.println("Sleeping for 10s");
						Thread.sleep(10000); //todo 30s
						syncChosenOnes(); //Receive actualMiner from nodes receiveActualMiner();
						System.out.println("lista");
						for(NonMinedBlock m : nonMinedBlocks) {
							System.out.println(m.ip + " " + m.magicNumberCount);
						}
						actualMiner = chooseActualMiner();
						resetNodes();
						addEveryoneExcept(actualMiner);

						Block minedblock;

						System.out.println("We have a miner: " + actualMiner);
						if (myTurnToMine()) {
							System.out.println("ME toca minar");
							syncVotes();
							while (votes.isEmpty()) { //todo carlos no lo ve seguro
								System.out.println("waiting for votes");
								Thread.sleep(1000);
								syncVotes();
							}
							minedblock = new Block(votes, getLastBlock());

							//Add block to ledger
							storeBlock(minedblock);

							//Delete votes and update voted users
							for(Vote v : minedblock.getVotes()) {
								database.putValue(v.getKey(), Database.State.Voted);
							}
							votes.clear();

							//Send block to everyone
							System.out.println("enviando bloque");
							NodeHandler.sendBlockToAll(minedblock);
						}
						else
							syncBlock();

					} catch (InterruptedException ignored) {} catch (IOException | InvalidKeySpecException e) {
						throw new RuntimeException(e);
					}
				}
			}
		});
		thread.start();
	}

	private class NonMinedBlock {
		private final String ip;
		private int nonMinedBlocks;
		private int magicNumberCount;

		public NonMinedBlock(String ip, int nonMinedBlocks, int magicNumberCount) {
			this.ip = ip;
			this.nonMinedBlocks = nonMinedBlocks;
			this.magicNumberCount = magicNumberCount;
		}

		public String getIp() {
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

		@Override
		public String toString() {
			return ip + ", " + nonMinedBlocks + ", " + magicNumberCount + "\n";
		}
	}

	private final ArrayList<String> bootstrapNodes = new ArrayList<>();
	private final ClientListener userListener;
	private final NodeListener nodeListener;
	private String chosenMiner = null; //todo cambiar a String
	private String actualMiner = null; //todo cambiar a String
	private final ArrayList<NonMinedBlock> nonMinedBlocks = new ArrayList<>();
	private final ArrayList<Vote> votes;
	private final ArrayList<Block> blocks;
	Database database;


}
