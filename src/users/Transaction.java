package users;

import utils.HashUtils;

import java.io.Serializable;
import java.security.*;

public class Transaction implements Serializable, Comparable<Transaction> {
    private final String transactionString;
    private final byte[] signature;
    private final PublicKey key;
    private final long time;

    public Transaction(String transaction, byte[] signature, PublicKey key) {
        this.transactionString = transaction;
        this.signature = signature;
        this.key = key;
        this.time = System.currentTimeMillis();
    }

    public String getTransactionString() {
        return transactionString;
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
    public int compareTo(Transaction transaction) {
        return Long.compare(this.getTime(), transaction.getTime());
    }

    public static byte[] sign(String transaction, PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(key);

        byte[] bytes = transaction.getBytes();
        signature.update(bytes);
        return signature.sign();
    }

    public static boolean verify(Transaction transaction) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(transaction.getKey());

        byte[] bytes = transaction.getTransactionString().getBytes();
        signature.update(bytes);
        return signature.verify(transaction.getSignature())
                && verifyTransaction(getAddress(transaction.getTransactionString()), transaction.getKey());
    }

    private static boolean verifyTransaction(String address, PublicKey key) {
        return address.equals(HashUtils.hash(key.toString()));
    }

    private static String getAddress(String transaction) {
        return transaction.substring(0, transaction.indexOf(' '));
    }

    public String displayTransaction() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        return  "\nTransaction : " + getTransactionString() +
                "\nTime        : " + getTime() +
                "\nSignature   : " + HashUtils.toHexString(getSignature()) +
                "\nPublic Key  : " + getKey() +
                "\nVerified    : " + Transaction.verify(this);
    }

    public String displayTransactionShort() {
        String shortAddress = getTransactionString().substring(0, 8);
        String vote = getTransactionString().substring(getTransactionString().indexOf(' '), getTransactionString().length());
        return shortAddress + vote;
    }
}
