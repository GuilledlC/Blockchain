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
import java.net.Socket;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class Node {

	public Node() throws IOException, InterruptedException  {
		this.votes = new ArrayList<>();
		this.database = new Database("votesCheck");

		this.blocks = new ArrayList<>();
		chooseBlockchain();

		this.userListener = new ClientListener(8888);
		Thread userThread = new Thread(this.userListener);
		userThread.start();
		this.nodeListener = new NodeListener(9999);
		Thread nodeThread = new Thread(this.nodeListener);
		nodeThread.start();

		initializeBootstrapNodes();
		//todo connect to every node
		connectToBootstrapNodes();
		setNonMinedBlocks(bootstrapNodes);

		nodeExecution();
	}

	private void connectToBootstrapNodes() {
		for(InetAddress address : bootstrapNodes) {
			try {
				if(!NodeHandler.isConnectedTo(address)) {
					NodeHandler peer = new NodeHandler(new Socket(address, 9999));
					Thread peerThread = new Thread(peer);
					peerThread.start();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void initializeBootstrapNodes() {
		//bootstrapNodes.add(new InetSocketAddress("localhost", 9999).getAddress());
		bootstrapNodes.add(new InetSocketAddress("88.27.144.170", 9999).getAddress());
		bootstrapNodes.add(new InetSocketAddress("2.153.80.40", 9999).getAddress());
	}

	private void chooseBlockchain() {
		Ledger.dropBlocks();

		ArrayList<Block> chosenBlockchain = new ArrayList<>();
		NodeHandler.requestBlockchainS();
		ArrayList<ArrayList<Block>> blockchainS = NodeHandler.getBlockchainS();
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
		ArrayList<InetAddress> tempChosenOnes = NodeHandler.getChosenOnes();
		tempChosenOnes.add(chosenMiner);
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
		if (correctBlock(block)) {
			storeBlock(block);
			for(Vote v : block.getVotes()) {
				votes.remove(v);
				database.putValue(v.getKey(), Database.State.Voted);
			}
		}
		else
			punishNode(actualMiner);
	}

	public ArrayList<Block> getBlocks() {
		return blocks;
	}

	private void setNonMinedBlocks(ArrayList<InetAddress> sockets) {
		for (InetAddress ip : sockets)
			this.nonMinedBlocks.add(new NonMinedBlock(ip, 1, 0));
		this.nonMinedBlocks.sort(new Comparator<NonMinedBlock>() {
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

	private void nodeExecution() throws InterruptedException {
		Thread mineThread = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("Start mining thread");
				while (true) {
					try {
						Block minedblock;
						if (actualMiner != null) {
							System.out.println("We have a miner");
							if (myTurnToMine()) {
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
								NodeHandler.sendBlockToAll(minedblock);
							}
							else
								syncBlock();

							actualMiner = null;

						} else {
							System.out.println("Sleeping for 5s");
							Thread.sleep(5000);
						}
					} catch (InterruptedException ignored) {} catch (IOException | InvalidKeySpecException e) {
						throw new RuntimeException(e);
					}
				}
			}
		});
		mineThread.start();

		Thread minerElection = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("Start election thread");
				while (true) {
					try {
						//Para que no se elija minero mientras se está minando
						if(actualMiner == null) {
							Random random = new Random();
							int randomNumber = random.nextInt(0, getNonMinedBlocksModule());
							chosenMiner = proofOfConsensus(randomNumber);
							NodeHandler.sendChosenOneToAll(chosenMiner); //Send chosen miner to nodes
							System.out.println("Sleeping for 10s");
							Thread.sleep(10000); //todo 30s
							syncChosenOnes(); //Receive actualMiner from nodes receiveActualMiner();
							actualMiner = chooseActualMiner();
							resetNodes();
							addEveryoneExcept(actualMiner);
						} else
							Thread.sleep(1000);
					} catch (InterruptedException ignored) {}
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

	private final ArrayList<InetAddress> bootstrapNodes = new ArrayList<>();
	private final ClientListener userListener;
	private final NodeListener nodeListener;
	private static InetAddress ip = new InetSocketAddress("localhost", 9999).getAddress();
	private static InetAddress chosenMiner = null;
	private static InetAddress actualMiner = null;
	private final ArrayList<NonMinedBlock> nonMinedBlocks = new ArrayList<>();
	private final ArrayList<Vote> votes;
	private final ArrayList<Block> blocks;
	Database database;


}
