import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.RSAPublicKeySpec;

public class Client {
    private PrivateKey priv;
    private PublicKey pub;

    public Client() throws Exception {
        Init();
    }

    private void Init() throws Exception {
        CheckKeys();
    }

    private void CheckKeys() throws Exception {
        if(Files.exists(Paths.get("./key.key"))) {
            priv = KeyUtils.PrivateKeyReader("./key.key");
            if(Files.exists(Paths.get("./key.pub")))
                pub = KeyUtils.PublicKeyReader("./key.pub");
            else
                pub = KeyUtils.PublicKeyFromPrivate(priv);
        } else {
            KeyPair keyPair = KeyUtils.KeyPairGenerator();
            priv = keyPair.getPrivate();
            KeyUtils.SaveKey(priv, "key.key");
            pub = keyPair.getPublic();
            KeyUtils.SaveKey(pub, "key.pub");
        }
    }
}
