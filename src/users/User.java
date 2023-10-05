package users;

import sockets.networkUser;
import utils.HashUtils;
import utils.KeyUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class User extends networkUser {

    private final String uid;
    private PrivateKey priv;
    private PublicKey pub;
    private String address;
    private Transaction transaction;
    private String privatePath;
    private String publicPath;

    public User(String userID) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        uid = userID;
        init();
    }

    private void init() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        privatePath = "./" + uid + ".key";
        publicPath = "./" + uid + ".pub";
        checkKeys();
        address = HashUtils.hash(pub.toString());
    }

    private void checkKeys() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        System.out.println("Checking for keys...");
        if(Files.exists(Paths.get(privatePath))) {
            System.out.println("Private key found");
            priv = KeyUtils.privateKeyReader(privatePath);
            if(Files.exists(Paths.get(publicPath))) {
                System.out.println("Public key found");
                pub = KeyUtils.publicKeyReader(publicPath);
            }
            else {
                System.out.println("Public key not found, generating public key...");
                pub = KeyUtils.publicKeyFromPrivate(priv);
                KeyUtils.saveKey(pub, publicPath);
            }
        } else {
            System.out.println("Keys not found, generating keys...");
            KeyPair keyPair = KeyUtils.keyPairGenerator();
            priv = keyPair.getPrivate();
            KeyUtils.saveKey(priv, privatePath);
            pub = keyPair.getPublic();
            KeyUtils.saveKey(pub, publicPath);
        }
        System.out.println("Done!\n");
    }

    protected void vote(String receiver) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        String transactionString = address + " " + receiver;
        byte[] signature = Transaction.sign(transactionString, priv);
        transaction = new Transaction(transactionString, signature, pub);
    }

    protected String getAddress() {
        return address;
    }

    protected Transaction getTransaction() {
        return transaction;
    }
}
