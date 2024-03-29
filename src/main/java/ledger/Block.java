package ledger;

import users.Vote;
import utils.HashUtils;

import java.util.ArrayList;
import java.util.Collection;

public class Block {

	private String hash;
	private String previousHash;
    private final ArrayList<Vote> votes;

    public Block() {
        this.votes = new ArrayList<>();
    }

    public Block(Collection<Vote> votes) {
        this();
        this.votes.addAll(votes);
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

	public byte[] getMerkleRoot() {
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
	}

}
