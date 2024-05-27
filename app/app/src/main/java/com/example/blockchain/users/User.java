package com.example.blockchain.users;

import android.os.Build;

import com.example.blockchain.sockets.NetworkUser;
import utils.HashUtils;
import utils.KeyUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class User extends NetworkUser {

    private PrivateKey priv;
    private PublicKey pub;
    private String address;
    private Vote vote;
    private String privatePath;
    private String publicPath;

    public User(String id) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        super(id);
        init();
    }

    private void init() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        privatePath = "./" + id + ".key";
        publicPath = "./" + id + ".pub";
        checkKeys();
        address = HashUtils.hash(pub.toString());
    }

    private void checkKeys() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        System.out.println("Checking for keys...");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(Files.exists(Paths.get(privatePath))) {
                System.out.println("Private key found");
                //priv = KeyUtils.privateKeyReader(privatePath);
                if(Files.exists(Paths.get(publicPath))) {
                    System.out.println("Public key found");
                    //pub = KeyUtils.publicKeyReader(publicPath);
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
        }
        System.out.println("Done!\n");
    }

    protected void vote(String receiver) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        String voteString = address + " " + receiver;
        byte[] signature = Vote.sign(voteString, priv);
        vote = new Vote(voteString, signature, pub);
        sendObject(vote);
    }

    protected String getAddress() {
        return address;
    }

    protected Vote getVote() {
        return vote;
    }
}
