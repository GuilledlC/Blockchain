package com.example.blockchain.ledger;

import com.google.common.primitives.Bytes;
import com.example.blockchain.users.Vote;
import com.example.blockchain.utils.HashUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class Block implements Serializable {

	private final byte[] hash;
	private final byte[] previousHash;
	private final Long youngestVote;
	private final Long oldestVote;
    private final ArrayList<Vote> votes = new ArrayList<>();

	public static Block getGenesis() {
		return new Block();
	}

	//Genesis
	private Block() { //todo
		this.hash = HashUtils.hash(("https://www.juntaelectoralcentral.es/cs/jec/eleccionesEnCurso/%20Europeas_junio2024").getBytes());
		this.previousHash = null;
		this.youngestVote = 0L;
		this.oldestVote = 0L;
	}

	//todo arreglar que pasa cuando no hay votos (youngestVote, oldestvote)
    public Block(ArrayList<Vote> votes, Block previous) {
        this.votes.addAll(votes);
		Collections.sort(this.votes);
		this.previousHash = previous.hash;
		this.youngestVote = votes.get(0).getTime();
		this.oldestVote = votes.get(votes.size() - 1).getTime();
		this.hash = HashUtils.hash(Bytes.concat(previousHash, ("" + youngestVote + oldestVote + this.votes).getBytes()));
    }

	//Copy
	public Block(Block copy) {
		this.votes.addAll(copy.votes);
		this.previousHash = copy.previousHash;
		this.youngestVote = copy.youngestVote;
		this.oldestVote = copy.oldestVote;
		this.hash = copy.hash;
	}

    public String displayBlock() {
        StringBuilder block = new StringBuilder("\nNumber of votes in block: " + votes.size());
        for(Vote t: votes) {
            block.append("\n").append(t.displayVoteShort());
        }
        return block.toString();
    }

    public ArrayList<Vote> getVotes() {
        return votes;
    }

	public byte[] getHash() {
		return hash;
	}

	public byte[] getPreviousHash() {
		return previousHash;
	}

	public boolean isTimeBetweenVotes(long time) {
		return time >= youngestVote && time <= oldestVote;
	}

	//todo merkle root
	/*public byte[] getMerkleRoot() {
		ArrayList<byte[]> txids = new ArrayList<>();
		for(Vote vote : votes)
			txids.add(vote.getTXID());
		return MerkleTree.getMerkleRoot(txids);
	}

	static class MerkleTree {

		public static byte[] getMerkleRoot(ArrayList<byte[]> txids) {
			ArrayList<byte[]> merkleRoot = merkleTree(txids);
			return merkleRoot.get(0);
		}

		private static ArrayList<byte[]> merkleTree(ArrayList<byte[]> hashList){

			//If we only have one leaf, return
			if(hashList.size() == 1)
				return hashList;

			ArrayList<byte[]> parentHashList = new ArrayList<>();

			//Hash the leaf transaction pair to get the parent transaction
			for(int i = 0; i < hashList.size(); i += 2){
				byte[] hashedPair = HashUtils.hash(HashUtils.concat(hashList.get(i), hashList.get(i+1)));
				parentHashList.add(hashedPair);
			}

			//If there is an odd number of transactions, add the last transaction again
			if(hashList.size() % 2 == 1){
				byte[] lastHash = hashList.get(hashList.size() - 1);
				byte[] hashedPair = HashUtils.hash(HashUtils.concat(lastHash, lastHash));
				parentHashList.add(hashedPair);
			}
			return merkleTree(parentHashList);
		}
	}*/

}
