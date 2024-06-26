package com.example.blockchain.old.users;

import com.example.blockchain.users.Vote;
import com.example.blockchain.old.sockets.OldNetworkUser;
import com.example.blockchain.utils.HashUtils;
import com.example.blockchain.utils.KeyUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class OldUserOld extends OldNetworkUser {

    private PrivateKey priv;
    private PublicKey pub;
    private byte[] address;
    private Vote vote;
    private String privatePath;
    private String publicPath;

    public OldUserOld(String id) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        super(id);
        init();
    }

    private void init() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        privatePath = "./" + id + ".key";
        publicPath = "./" + id + ".pub";
        checkKeys();
        address = HashUtils.hashString(pub.toString());
    }

    private void checkKeys() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        System.out.println("Checking for keys...");
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
        System.out.println("Done!\n");
    }

    protected void vote(String receiver) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        String voteString = HashUtils.toHexString(address) + " " + receiver;
        byte[] signature = Vote.sign(voteString, priv);
        vote = new Vote(/*address, */voteString, signature, pub);
        sendObject(vote);
    }

    protected byte[] getAddress() {
        return address;
    }

    protected Vote getVote() {
        return vote;
    }
}
