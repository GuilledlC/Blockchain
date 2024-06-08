import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {

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