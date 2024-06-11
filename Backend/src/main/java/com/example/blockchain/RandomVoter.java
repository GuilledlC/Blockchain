package com.example.blockchain;

import com.example.blockchain.database.Database;
import com.example.blockchain.users.User;
import com.example.blockchain.utils.KeyUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

public class RandomVoter {

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException, InterruptedException {
		for(int i = 0; i < 100; i++) {
			PublicKey pubk = KeyUtils.publicKeyReader(Files.readAllBytes(Paths.get("keys/" + i + ".pub")));
			PrivateKey privk = KeyUtils.privateKeyReader(Files.readAllBytes(Paths.get("keys/" + i + ".priv")));
			User user = new User(privk, pubk);
			user.vote("" + i);
			Thread.sleep((new Random()).nextInt(0, 5) * 1000L);
		}
	}

}
