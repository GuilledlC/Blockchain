package users;

import utils.HashUtils;
import utils.KeyUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class User {

    private final String uid;
    private PrivateKey priv;
    private PublicKey pub;
    private String address;
    private final String privatePath;
    private final String publicPath;

    public User(String userID) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        uid = userID;
        privatePath = "./" + uid + ".key";
        publicPath = "./" + uid + ".pub";
        init();
    }

    private void init() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        checkKeys();
        address = HashUtils.hash(pub.toString());
    }

    private void checkKeys() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        if(Files.exists(Paths.get(privatePath))) {
            priv = KeyUtils.privateKeyReader(privatePath);
            if(Files.exists(Paths.get(publicPath)))
                pub = KeyUtils.publicKeyReader(publicPath);
            else {
                pub = KeyUtils.publicKeyFromPrivate(priv);
                KeyUtils.saveKey(pub, publicPath);
            }
        } else {
            KeyPair keyPair = KeyUtils.keyPairGenerator();
            priv = keyPair.getPrivate();
            KeyUtils.saveKey(priv, privatePath);
            pub = keyPair.getPublic();
            KeyUtils.saveKey(pub, publicPath);
        }
    }

    public Transaction vote(String receiver) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        String transaction = address + " " + receiver;
        byte[] signature = Transaction.sign(transaction, priv);
        return new Transaction(transaction, signature, pub);
    }

    public String getAddress() {
        return address;
    }
}
