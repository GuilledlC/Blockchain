import Utils.HashUtils;

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

    public static byte[] Sign(String text, PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(key);

        byte[] bytes = text.getBytes();
        signature.update(bytes);
        return signature.sign();
    }

    public static boolean Verify(String text, byte[] signed, PublicKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(key);

        byte[] bytes = text.getBytes();
        signature.update(bytes);
        return signature.verify(signed) && VerifyTransaction(GetAddress(text), key);
    }

    private static boolean VerifyTransaction(String address, PublicKey key) {
        return address.equals(HashUtils.Hash(key.toString()));
    }

    private static String GetAddress(String transaction) {
        return transaction.substring(0, transaction.indexOf(' '));
    }

}
