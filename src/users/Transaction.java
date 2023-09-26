package users;

import utils.HashUtils;
import java.security.*;

public class Transaction {
    private final String transaction;
    private final byte[] signature;
    private final PublicKey key;

    public Transaction(String transaction, byte[] signature, PublicKey key) {
        this.transaction = transaction;
        this.signature = signature;
        this.key = key;
    }

    public String getTransaction() {
        return transaction;
    }

    public byte[] getSignature() {
        return signature;
    }

    public PublicKey getKey() {
        return key;
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

        byte[] bytes = transaction.getTransaction().getBytes();
        signature.update(bytes);
        return signature.verify(transaction.getSignature())
                && verifyTransaction(getAddress(transaction.getTransaction()), transaction.getKey());
    }

    private static boolean verifyTransaction(String address, PublicKey key) {
        return address.equals(HashUtils.hash(key.toString()));
    }

    private static String getAddress(String transaction) {
        return transaction.substring(0, transaction.indexOf(' '));
    }

}
