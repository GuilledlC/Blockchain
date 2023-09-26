package Users;

import Utils.HashUtils;
import Utils.KeyUtils;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;

public class User {

    private final String uid;
    private PrivateKey priv;
    private PublicKey pub;
    private String address;
    private final String privatePath;
    private final String publicPath;

    public User(String userID) throws Exception {
        uid = userID;
        privatePath = "./" + uid + ".key";
        publicPath = "./" + uid + ".pub";
        Init();
    }

    private void Init() throws Exception {
        CheckKeys();
        address = HashUtils.Hash(pub.toString());
    }

    private void CheckKeys() throws Exception {
        if(Files.exists(Paths.get(privatePath))) {
            priv = KeyUtils.PrivateKeyReader(privatePath);
            if(Files.exists(Paths.get(publicPath)))
                pub = KeyUtils.PublicKeyReader(publicPath);
            else {
                pub = KeyUtils.PublicKeyFromPrivate(priv);
                KeyUtils.SaveKey(pub, publicPath);
            }
        } else {
            KeyPair keyPair = KeyUtils.KeyPairGenerator();
            priv = keyPair.getPrivate();
            KeyUtils.SaveKey(priv, privatePath);
            pub = keyPair.getPublic();
            KeyUtils.SaveKey(pub, publicPath);
        }
    }

    public Transaction Vote(String receiver) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        String transaction = address + " " + receiver;
        byte[] signature = Transaction.Sign(transaction, priv);
        return new Transaction(transaction, signature, pub);
    }

    public String getAddress() {
        return address;
    }
}
