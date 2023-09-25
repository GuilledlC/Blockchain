import java.security.PublicKey;

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
}
