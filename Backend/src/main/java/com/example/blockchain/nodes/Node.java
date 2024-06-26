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

	public Node() throws IOException, InterruptedException {
		this.votes = new ArrayList<>();
		this.database = new Database("votesCheck");

		this.userListener = new ClientListener(8888, this);
		Thread userThread = new Thread(this.userListener);
		userThread.start();

		this.nodeListener = new NodeListener(9999);
		Thread nodeThread = new Thread(this.nodeListener);
		nodeThread.start();

		initializeBootstrapNodes();
		connectToBootstrapNodes();

		blockBarrier();
		System.out.println("\nTime barrier terminated\n");

		setNonMinedBlocks(bootstrapNodes);
		chooseBlockchain();

		nodeExecution();
	}


	private void initializeBootstrapNodes() {
		bootstrapNodes.add("80.39.151.138");
		bootstrapNodes.add("86.127.225.89");
		bootstrapNodes.add("80.102.1.93");

		bootstrapNodes.remove(NetworkVariables.ip);
	}

	private void connectToBootstrapNodes() {
		for(String address : bootstrapNodes) {
			nodeListener.connectTo(address);
		}
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



	public static void minerBarrier() throws InterruptedException {

		LocalTime ahora = LocalTime.now(), objetivo;
		objetivo = ahora.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);

		while(LocalTime.now().isBefore(objetivo)) {
			Thread.sleep(100);
		}
	}

	public static void blockBarrier() throws InterruptedException {

		LocalTime ahora = LocalTime.now(), objetivo;
		objetivo = ahora.truncatedTo(ChronoUnit.MINUTES).plusSeconds(30);

		while(LocalTime.now().isBefore(objetivo)) {
			Thread.sleep(100);
		}
	}



	private void chooseBlockchain() {
		Ledger.dropBlocks();

		String chosenIP = "";
		NodeHandler.requestHashS();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		HashMap<byte[], String> hashes = NodeHandler.getHashes();
		HashMap<byte[], Integer> election = new HashMap<>();

		//Por cada una de los hashes que tengo (que asumo que estan ordenadas)
		for (byte[] hash : hashes.keySet()) {
			//Si no existe lo apunto
			if(!election.containsKey(hash))
				election.put(hash, 0);
			//Le voto
			election.put(hash, election.get(hash) + 1);
		}

		int max = 0;
		for(byte[] hash : hashes.keySet()) {
			int num = election.get(hash);
			if(num > max) {
				max = num;
				chosenIP = hashes.get(hash);
			}
		}

		if(max == 0)
			storeBlock(Block.getGenesis());
		else
			NodeHandler.requestBlockchain(chosenIP);
		NodeHandler.getHashes(); //Esto sirve para vaciar el buffer de NodeHandler
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
						blockBarrier();
					} catch (InterruptedException ignored) {} catch (IOException | InvalidKeySpecException e) {
						throw new RuntimeException(e);
					}
				}
			}
		});
		thread.start();
	}


	private void minerElection() throws IOException, InvalidKeySpecException, InterruptedException {
		System.out.println("\n");
		Random random = new Random();
		int randomNumber = random.nextInt(0, getNonMinedBlocksModule());
		chosenMiner = proofOfConsensus(randomNumber);
		NodeHandler.sendMagicNumberToAll(chosenMiner); //Send chosen miner to nodes
		System.out.println("Waiting for barrier");
		minerBarrier(); //todo 30s
		syncMagicNumbers(); //Receive actualMiner from nodes receiveActualMiner();
		System.out.println("\n\nRecolectingVotes\n");
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


	private int getNonMinedBlocksModule() {
		int aux = 0;
		for (NonMinedBlock item : nonMinedBlocks){
			aux += item.getNonMinedBlocks();
		}
		return aux;
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


	private void addEveryoneExcept(String ip){
		for (NonMinedBlock item : nonMinedBlocks) {
			if (item.getNonMinedBlocks() != 0) {
				item.setNonMinedBlocks(item.getNonMinedBlocks() + 1);
				if (item.getIp().equals(ip))
					item.setNonMinedBlocks(item.getNonMinedBlocks() - 1);
			}
		}
	}

	private void resetMagicNumbers() {
		for (NonMinedBlock item : nonMinedBlocks)
			item.setMagicNumberCount(0);
	}


	private void syncMagicNumbers() {
		ArrayList<String> tempChosenOnes = NodeHandler.getMagicNumbers();
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

	private void syncVotes() throws IOException, InvalidKeySpecException {
		ArrayList<Vote> tempVotes = new ArrayList<>();
		tempVotes.addAll(ClientHandler.getVotes());
		tempVotes.addAll(NodeHandler.getVotes());

		for (Vote v : tempVotes) {
			if(database.exists(v.getKey()) && Vote.verifyVote(v)) {
				votes.add(v);
				database.putValue(v.getKey(), Database.State.InPool);
			}
		}
	}

	private void syncBlock() throws IOException, InvalidKeySpecException, InterruptedException {
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
					database.putValue(v.getKey(), Database.State.Voted);
				}
			}
		}
		addEveryoneExcept(actualMiner);
	}


	private void punishNode(String ip) {
		for (NonMinedBlock item : nonMinedBlocks){
			if (item.getIp().equals(ip))
				item.setNonMinedBlocks(0);
		}
		System.out.println("castigueited");
	}

	private boolean myTurnToMine(){
		return NetworkVariables.ip.equals(actualMiner);
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
		NodeHandler.sendBlockToAll(minedblock);
	}


	private boolean correctBlock (Block block) throws IOException, InvalidKeySpecException {
		/**La comprobacion se basará en comprobar si el hash del bloque anterior
		 * es igual que el del bloque anterior de la blockchain del nodo,
		 * si cada voto pertenece a un votante que no ha votado aun
		 * y comprobar que los votos sean correctos (que la firma funcione con la publica).**/
		if(block == null)
			return false;
		ArrayList<Vote> tempVotes = block.getVotes();
		boolean aux = Arrays.equals(block.getPreviousHash(), getLastBlock().getHash()) && Block.verifyBlock(block);
		int count = 0;
		while (aux && count < tempVotes.size()) {
			Vote tempVote = tempVotes.get(count);

			try {
				aux = !database.hasVoted(tempVote.getKey());
			} catch (NullPointerException e) {
				aux = false;
			}
			aux = aux && Vote.verifyVote(tempVote);
			count++;
		}
		return aux;
	}

	private void storeBlocks(ArrayList<Block> blocks) {
		Ledger.storeBlocks(blocks);
	}

	private void storeBlock(Block block) {
		Ledger.storeBlock(block);
	}

	private Block getLastBlock() {
		return Ledger.getLastBlock();
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

	public boolean checkDatabase(byte[] publicKey) {
		try {
			return this.database.hasVoted(publicKey);
		} catch (NullPointerException e) {
			return false;
		}
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


	private final ArrayList<Vote> votes;
	private final ArrayList<String> bootstrapNodes = new ArrayList<>();
	private final ClientListener userListener;
	private final NodeListener nodeListener;
	private String chosenMiner = null;
	private String actualMiner = null;
	private final ArrayList<NonMinedBlock> nonMinedBlocks = new ArrayList<>();
	Database database;


}
