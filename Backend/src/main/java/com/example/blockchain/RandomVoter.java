package com.example.blockchain;

import com.example.blockchain.users.User;
import com.example.blockchain.utils.KeyUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class RandomVoter {
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException, InterruptedException {
		/*for(int i = 0; i < 100000; i++) {
			PublicKey pubk = KeyUtils.publicKeyReader(
					Files.readAllBytes(Paths.get("keys/" + i + ".pub")));
			PrivateKey privk = KeyUtils.privateKeyReader(
					Files.readAllBytes(Paths.get("keys/" + i + ".key")));
			User user = new User(privk, pubk);
			user.vote("" + i);
		}*/

		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		IntStream.range(0, 100000)
				.forEach(i -> CompletableFuture.supplyAsync(() -> {
					try {
						PublicKey pubk = KeyUtils.publicKeyReader(
								Files.readAllBytes(Paths.get("keys/" + i + ".pub")));
						PrivateKey privk = KeyUtils.privateKeyReader(
								Files.readAllBytes(Paths.get("keys/" + i + ".key")));
						User user = new User(privk, pubk);
						user.vote("" + i);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}, executor));

		executor.shutdown();
	}
}
