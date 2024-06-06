package com.example.blockchain.users;

import com.example.blockchain.utils.HashUtils;

import java.io.Serializable;
import java.security.*;
import java.util.Arrays;

public class Vote implements Serializable, Comparable<Vote> {
    private final String voteString;
    private final byte[] signature;
    private final PublicKey key;
    private final long time;

    public Vote(String vote, byte[] signature, PublicKey key) {
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

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Vote vote)
            return this.getKey().equals(vote.getKey());
        else
            return super.equals(obj);
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
        return signature.verify(vote.getSignature());
    }

    public String displayVote() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        return  "\nVote        : " + getVoteString() +
                "\nTime        : " + getTime() +
                "\nSignature   : " + HashUtils.toHexString(getSignature()) +
                "\nPublic Key  : " + getKey() +
                "\nVerified    : " + Vote.verify(this);
    }

    public String displayVoteShort() {
        return getVoteString().substring(getVoteString().indexOf(' '));
    }

    public byte[] getTXID() {
        String dataToHash = voteString + Arrays.toString(signature) + key.toString() + time;
        return HashUtils.hashString(dataToHash);
    }
}
