package com.example.blockchain.users;

import com.example.blockchain.utils.HashUtils;
import com.example.blockchain.utils.KeyUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.io.Serializable;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class Vote implements Serializable, Comparable<Vote> {

	public Vote(String vote, byte[] signature, PublicKey key) {
		this.voteString = vote;
		this.signature = signature;
		this.key = key.getEncoded();
		this.time = System.currentTimeMillis();
	}

	@JsonCreator
	public Vote(
			@JsonProperty("voteString") String voteString,
			@JsonProperty("signature") byte[] signature,
			@JsonProperty("key") byte[] key,
			@JsonProperty("time") long time) {
		this.voteString = voteString;
		this.signature = signature;
		this.key = key;
		this.time = time;
	}


	public String getVoteString() {
		return voteString;
	}

	public byte[] getSignature() {
		return signature;
	}

	public byte[] getKey() {
		return key;
	}

	public long getTime() {
		return time;
	}

	public static byte[] signVote(String vote, PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(key);

		byte[] bytes = vote.getBytes();
		bytes = HashUtils.hash(bytes);
		signature.update(bytes);
		return signature.sign();
	}

	public static boolean verifyVote(Vote vote) throws IOException, InvalidKeySpecException {
		try {
			Signature signature = Signature.getInstance("SHA256withRSA");
			signature.initVerify(KeyUtils.publicKeyReader(vote.getKey()));

			byte[] bytes = vote.getVoteString().getBytes();
			bytes = HashUtils.hash(bytes);
			signature.update(bytes);
			return signature.verify(vote.getSignature());
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			return false;
		}
	}

	public String displayVote() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, IOException, InvalidKeySpecException {
		return  "\nVote        : " + getVoteString() +
				"\nTime        : " + getTime() +
				"\nSignature   : " + HashUtils.toHexString(getSignature()) +
				"\nPublic Key  : " + Arrays.toString(getKey()) +
				"\nVerified    : " + Vote.verifyVote(this);
	}

	public String displayVoteShort() {
		return getVoteString().substring(getVoteString().indexOf(' '));
	}

	@Override
	public int compareTo(Vote vote) {
		return Long.compare(this.getTime(), vote.getTime());
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Vote vote)
			return Arrays.equals(this.getKey(), vote.getKey());
		else
			return super.equals(obj);
	}
	@Override
	public String toString() {
		try {
			return displayVote();
		} catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException | IOException |
				 InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
	}


	private final String voteString;
	private final byte[] signature;
	private final byte[] key;
	private final long time;


}
