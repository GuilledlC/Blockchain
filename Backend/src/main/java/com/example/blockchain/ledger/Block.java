package com.example.blockchain.ledger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.primitives.Bytes;
import com.example.blockchain.users.Vote;
import com.example.blockchain.utils.HashUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Block implements Serializable {


	public static Block getGenesis() {
		return new Block();
	}

	public static boolean verifyBlock(Block block) {
		byte[] hash = HashUtils.hash(Bytes.concat(block.previousHash, ("" + block.youngestVote + block.oldestVote + block.votes).getBytes()));
		return Arrays.equals(block.hash, hash);
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

	@JsonCreator
	public Block(
			@JsonProperty("hash") byte[] hash,
			@JsonProperty("previousHash") byte[] previousHash,
			@JsonProperty("youngestVote") Long youngestVote,
			@JsonProperty("oldestVote") Long oldestVote,
			@JsonProperty("votes") ArrayList<Vote>  votes) {
		this.hash = hash;
		this.previousHash = previousHash;
		this.youngestVote = youngestVote;
		this.oldestVote = oldestVote;
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

	public byte[] getHash() {
		return hash;
	}

	public byte[] getPreviousHash() {
		return previousHash;
	}

	public Long getYoungestVote() {
		return youngestVote;
	}

	public Long getOldestVote() {
		return oldestVote;
	}

	public boolean isTimeBetweenVotes(long time) {
		return time >= youngestVote && time <= oldestVote;
	}


	private final byte[] hash;
	private final byte[] previousHash;
	private final Long youngestVote;
	private final Long oldestVote;
	private final ArrayList<Vote> votes = new ArrayList<>();

}
