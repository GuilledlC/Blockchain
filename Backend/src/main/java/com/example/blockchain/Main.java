package com.example.blockchain;

import com.example.blockchain.database.Database;
import com.example.blockchain.nodes.Node;
import com.example.blockchain.utils.KeyUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;

public class Main {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException {

		saveKeys();
		Node node = new Node();
    }

	private static void saveKeys() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		Database database = new Database("votesCheck");
		for(int i = 0; i < 100; i++) {
			PublicKey pubk = KeyUtils.publicKeyReader(Files.readAllBytes(Paths.get("keys/" + i + ".pub")));
			database.putValue(pubk.getEncoded(), Database.State.Exists);
		}
		database.closeDatabase();
	}

    private static void generateData(File dnis) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(dnis));
        for(int i = 10000000; i < 30000000; ++i) {
            String dni = i + "E\n";
            bw.write(dni);
        }
        bw.close();
    }
}