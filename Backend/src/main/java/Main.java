import database.Database;
import org.iq80.leveldb.DBIterator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        try {
			Database votesCheck = new Database("votesCheck");
            //Database poolVotes = new Database("poolVotes");

            File file = new File("publicKeys.txt");

            //generateData(file);
            //votesCheck.loadData(file);



            long millis = System.currentTimeMillis();
            //getValues(votesCheck);

            votesCheck.changeValue("27384915E", "0");
            System.out.println(votesCheck.hasVoted("27384915E"));
            votesCheck.changeValue("27384915E", "1");
            System.out.println(votesCheck.hasVoted("27384915E"));
            votesCheck.changeValue("27384915E", "2");
            System.out.println(votesCheck.hasVoted("27384915E"));

            long millis2 = System.currentTimeMillis();
            System.out.println(millis2 - millis);

            votesCheck.closeDatabase();
            //poolVotes.closeDatabase();
        } catch (IOException error){
            error.printStackTrace();
        }
    }

    private static void generateData(File dnis) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(dnis));
        for(int i = 10000000; i < 30000000; ++i) {
            String dni = i + "E\n";
            bw.write(dni);
        }
        bw.close();
    }

    /** FOR ANGUS
    private static boolean getValues(Database db) {
        Random rand = new Random();
        ArrayList<String> votantes = new ArrayList<>();
        try {
            for(String v : votantes){
                if (!db.hasVoted(v)){
                    db.changeValue("pibe", true);
                } else
                    throw new NullPointerException();
            }
            return true;
        }catch (NullPointerException e) {
            System.out.println("nodo malo");
            return false;
        }
    }**/
}