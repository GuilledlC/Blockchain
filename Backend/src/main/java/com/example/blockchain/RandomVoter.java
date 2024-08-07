package com.example.blockchain;

import com.example.blockchain.users.User;
import com.example.blockchain.users.Vote;
import com.example.blockchain.utils.KeyUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class RandomVoter {
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException, InterruptedException, ExecutionException {

		User user = new User(
				KeyUtils.privateKeyReader(Files.readAllBytes(Paths.get("keys/Guille.key"))),
				KeyUtils.publicKeyReader(Files.readAllBytes(Paths.get("keys/Guille.pub")))
		);

		int numVotes = 100;
		int availableProcessors = Runtime.getRuntime().availableProcessors();
		ExecutorService executorService = Executors.newFixedThreadPool(availableProcessors);
		List<Callable<Vote>> tasks = new ArrayList<>();

		long t = System.currentTimeMillis();
		for (int i = 0; i < numVotes; i++) {
			final int index = i;
			tasks.add(() -> {
				PublicKey pubk = KeyUtils.publicKeyReader(
						Files.readAllBytes(Paths.get("keys/" + index + ".pub")));
				PrivateKey privk = KeyUtils.privateKeyReader(
						Files.readAllBytes(Paths.get("keys/" + index + ".key")));
				return user.createVote("" + index, privk, pubk);
			});
		}

		// Submit tasks and collect results
		List<Future<Vote>> futures = executorService.invokeAll(tasks);

		ArrayList<Vote> votes = new ArrayList<>();
		for (Future<Vote> future : futures) {
			votes.add(future.get());
		}

		System.out.println(votes.size() + " en " + (System.currentTimeMillis() - t) + "ms");
		System.out.println("Votos generados");
		Scanner s = new Scanner(System.in);
		s.nextLine();

		executorService.shutdown();

		for(int a = 0; a < 1; a++) {
			for (int i = 0; i < votes.size(); i++) {
				user.vote(votes.get(i));
				if (i % 100 == 0) {
					long t2 = System.currentTimeMillis();
					System.out.println("A" + i + " en " + (t2 - System.currentTimeMillis()) + "ms");
				}
			}
		}



		/*for(int i = 0; i < 100000; i++) {
			long t1 = System.currentTimeMillis();
			PublicKey pubk = KeyUtils.publicKeyReader(
					Files.readAllBytes(Paths.get("keys/" + i + ".pub")));
			PrivateKey privk = KeyUtils.privateKeyReader(
					Files.readAllBytes(Paths.get("keys/" + i + ".key")));
			User user = new User(privk, pubk);
			user.vote("" + i);
			System.out.println(System.currentTimeMillis() - t1);
		}*/

		/*ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		IntStream.range(0, 50000)
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

		executor.shutdown();*/
	}
}
