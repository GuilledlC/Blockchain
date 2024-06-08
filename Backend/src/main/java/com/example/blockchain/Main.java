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

	/*try {
			Database votesCheck = new Database("votesCheck");

            File file = new File("publicKeys.txt");
            //generateData(file);
            //votesCheck.loadData(file);

            long millis = System.currentTimeMillis();

            votesCheck.changeValue("27384915E", "0");
            System.out.println(votesCheck.hasVoted("27384915E"));
            votesCheck.changeValue("27384915E", "1");
            System.out.println(votesCheck.hasVoted("27384915E"));
            votesCheck.changeValue("27384915E", "2");
            System.out.println(votesCheck.hasVoted("0"));

            long millis2 = System.currentTimeMillis();
            System.out.println(millis2 - millis);

            votesCheck.closeDatabase();
        } catch (IOException error){
            error.printStackTrace();
        }*/


    /** FOR ANGUS
    private boolean getValues(ArrayList<Vote> votos) {
        boolean aux = false;
        int counter = 0;
        while (!aux)
            aux = db.hasVoted(votos.get(counter++).getKey());
        return !aux;
    }
     */
}