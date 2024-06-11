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
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;

public class Main {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException {
        Database database = new Database("votesCheck");
		PublicKey pubk = KeyUtils.publicKeyReader(Files.readAllBytes(Paths.get("keys/Carlos.pub")));
		database.putValue(pubk.getEncoded(), Database.State.Exists);
		pubk = KeyUtils.publicKeyReader(Files.readAllBytes(Paths.get("keys/Guille.pub")));
		database.putValue(pubk.getEncoded(), Database.State.Exists);
		database.closeDatabase();
		Node node = new Node();
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