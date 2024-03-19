import database.Database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
			Database votesCheck = new Database("votesCheck");
            Database poolVotes = new Database("poolVotes");

            File file = new File("./src/main/resources/publicKeys.txt");

            votesCheck.loadData(file);

            System.out.println(votesCheck.getValue("27384915E"));

            votesCheck.closeDatabase();
            poolVotes.closeDatabase();
        } catch (IOException error){
            error.printStackTrace();
        }
    }
}