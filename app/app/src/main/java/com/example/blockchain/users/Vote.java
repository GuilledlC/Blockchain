package com.example.blockchain.users;

import utils.HashUtils;

import java.io.Serializable;
import java.security.*;
import java.util.Arrays;

public class Vote implements Serializable, Comparable<Vote> {
	private final byte[] address;
    private final String voteString;
    private final byte[] signature;
    private final PublicKey key;
    private final long time;

    public Vote(byte[] address, String vote, byte[] signature, PublicKey key) {
        this.address = address;
		this.voteString = vote;
        this.signature = signature;
        this.key = key;
        this.time = System.currentTimeMillis();
    }

    public String getVoteString() {
        return voteString;
    }

    public byte[] getSignature() {
        return signature;
    }

    public PublicKey getKey() {
        return key;
    }

    public long getTime() {
        return time;
    }

    @Override
    public int compareTo(Vote vote) {
        return Long.compare(this.getTime(), vote.getTime());
    }

    public static byte[] sign(String vote, PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(key);

        byte[] bytes = vote.getBytes();
        signature.update(bytes);
        return signature.sign();
    }

    public static boolean verify(Vote vote) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(vote.getKey());

        byte[] bytes = vote.getVoteString().getBytes();
        signature.update(bytes);
        return signature.verify(vote.getSignature())
                && verifyVote(vote.getAddress(), vote.getKey());
    }

    private static boolean verifyVote(byte[] address, PublicKey key) {
        return Arrays.equals(address, HashUtils.hashString(key.toString()));
    }

    private byte[] getAddress() {
		return address;
    }

    public String displayVote() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        return  "\nVote        : " + getVoteString() +
                "\nTime        : " + getTime() +
                "\nSignature   : " + HashUtils.toHexString(getSignature()) +
                "\nPublic Key  : " + getKey() +
                "\nVerified    : " + Vote.verify(this);
    }

    public String displayVoteShort() {
        String address = HashUtils.toHexString(getAddress());
        String shortAddress = address.substring(0, 4) + "-" + address.substring(address.length() - 4);
        String vote = getVoteString().substring(getVoteString().indexOf(' '));
        return shortAddress + vote;
    }

	public byte[] getTXID() {
		String dataToHash = voteString + Arrays.toString(signature) + key.toString() + time;
		return HashUtils.hashString(dataToHash);
	}
}
