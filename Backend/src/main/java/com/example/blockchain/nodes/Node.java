package com.example.blockchain.nodes;

import com.example.blockchain.database.Database;
import com.example.blockchain.ledger.Block;
import com.example.blockchain.ledger.Ledger;
import com.example.blockchain.network.*;
import com.example.blockchain.users.Vote;

import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Node {


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

		initializeBootstrapNodes();
		connectToBootstrapNodes();

		timeBarrier();
		System.out.println("\nTime barrier terminated\n");

		setNonMinedBlocks(bootstrapNodes);

		chooseBlockchain();
		nodeExecution();
	}

	public static void timeBarrier() throws InterruptedException {

		LocalTime ahora = LocalTime.now(), objetivo;
		if(ahora.getSecond() < 30)
			objetivo = ahora.truncatedTo(ChronoUnit.MINUTES).plusSeconds(30);
		else
			objetivo = ahora.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);

		while(LocalTime.now().isBefore(objetivo)) {
			Thread.sleep(100);
		}
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

		bootstrapNodes.remove(NetworkVariables.ip);
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
		NodeHandler.getBlockchainS(); //Esto sirve para vaciar el buffer de NodeHandler
	}

	private void syncVotes() throws IOException, InvalidKeySpecException {
		ArrayList<Vote> tempVotes = new ArrayList<>();
		tempVotes.addAll(ClientHandler.getVotes());
		tempVotes.addAll(NodeHandler.getVotes());

		for (Vote v : tempVotes) {
			if(database.exists(v.getKey()) && Vote.verify(v)) {
				votes.add(v);
				database.putValue(v.getKey(), Database.State.InPool);
				NodeHandler.sendVoteToAll(v);
			}
		}
	}

	private void syncChosenOnes() {
		ArrayList<String> tempChosenOnes = NodeHandler.getChosenOnes();
		tempChosenOnes.add(chosenMiner);
		tempChosenOnes.sort(String::compareTo);
		int count = 0;
		for(NonMinedBlock item : nonMinedBlocks) {
			while(count < tempChosenOnes.size() && tempChosenOnes.get(count).equals(item.getIp())) {
				item.setMagicNumberCount(item.getMagicNumberCount() + 1);
				count++;
			}
		}
	}

	public ArrayList<Block> getBlocks() {
		return blocks;
	}

	private void setNonMinedBlocks(ArrayList<String> sockets) {
		for (String ip : sockets)
			this.nonMinedBlocks.add(new NonMinedBlock(ip, 1, 0));

		this.nonMinedBlocks.add(new NonMinedBlock(NetworkVariables.ip, 1, 0));
		this.nonMinedBlocks.sort(new Comparator<NonMinedBlock>() {
			@Override
			public int compare(NonMinedBlock o1, NonMinedBlock o2) {
				return o1.getIp().compareTo(o2.getIp());
			}
		});
	}

	private void addEveryoneExcept(String ip){
		for (NonMinedBlock item : nonMinedBlocks) {
			if (item.getNonMinedBlocks() != 0) {
				item.setNonMinedBlocks(item.getNonMinedBlocks() + 1);
				if (item.getIp().equals(ip))
					item.setNonMinedBlocks(item.getNonMinedBlocks() - 1);
			}
		}
	}

	private boolean correctBlock (Block block) throws IOException, InvalidKeySpecException {
		/**La comprobacion se basará en comprobar si el hash del bloque anterior
		 * es igual que el del bloque anterior de la blockchain del nodo,
		 * si cada voto pertenece a un votante que no ha votado aun
		 * y comprobar que los votos sean correctos (que la firma funcione con la publica).**/
		if(block == null)
			return false;
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

	private void resetMagicNumbers() {
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
		return NetworkVariables.ip.equals(actualMiner);
	}

	private void minerElection() throws IOException, InvalidKeySpecException, InterruptedException {
		System.out.println("\n");
		Random random = new Random();
		int randomNumber = random.nextInt(0, getNonMinedBlocksModule());
		chosenMiner = proofOfConsensus(randomNumber);
		NodeHandler.sendChosenOneToAll(chosenMiner); //Send chosen miner to nodes
		System.out.println("Sleeping for 10s");
		timeBarrier(); //todo 30s
		syncChosenOnes(); //Receive actualMiner from nodes receiveActualMiner();
		System.out.println("lista");
		for(NonMinedBlock m : nonMinedBlocks) {
			System.out.println(m.ip + " " + m.magicNumberCount);
		}
		actualMiner = chooseActualMiner();
		resetMagicNumbers();
		addEveryoneExcept(actualMiner);

		System.out.println("We have a miner: " + actualMiner);
		syncVotes();
	}

	private void mine() throws IOException, InvalidKeySpecException, InterruptedException {
		Block minedblock;
		System.out.println("ME toca minar");
		int count = 0;
		while (votes.isEmpty() && count++ <= 10) { //todo carlos no lo ve seguro
			System.out.println("waiting for votes");
			Thread.sleep(1000);
			syncVotes();
		}

		if(votes.isEmpty()) {
			NodeHandler.sendBlockToAll(null);
			return;
		}

		minedblock = new Block(votes, getLastBlock());

		//Delete votes and update voted users
		for(Vote v : minedblock.getVotes()) {
			database.putValue(v.getKey(), Database.State.Voted);
		}
		votes.clear();

		//Add block to ledger
		storeBlock(minedblock);

		//Send block to everyone
		System.out.println("enviando bloque");
		NodeHandler.sendBlockToAll(minedblock);
	}

	private void syncBlock() throws IOException, InvalidKeySpecException {
		//todo esperar 10s a que lo mande el nodo minero
		Block block = NodeHandler.getBlock();
		int count = 0;
		while (block == null && count++ <= 10) {
			try {
				Thread.sleep(1000); //todo interrumpir
				block = NodeHandler.getBlock();
			} catch (InterruptedException e) {throw new RuntimeException(e);}
		}

		if (block != null) {
			if(!correctBlock(block))
				punishNode(actualMiner);
			else {
				storeBlock(block);
				for(Vote v : block.getVotes()) {
					votes.remove(v);
					System.out.println("Borro votos");
					database.putValue(v.getKey(), Database.State.Voted);
				}
			}
		}
	}

	private void nodeExecution() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						minerElection();
						if (myTurnToMine())
							mine();
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
