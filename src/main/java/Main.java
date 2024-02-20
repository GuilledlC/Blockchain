import database.Database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
			Database database = new Database();
            /*File file = new File("./src/main/resources/publicKeys.txt");
            generateData(file);
            database.loadData(file);*/
            System.out.println(database.getValue("27384915E"));
            database.closeDatabase();
        } catch (IOException error){
            error.printStackTrace();
        }
    }

    private static void generateData(File dnis) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(dnis));
        for(int i = 10000000; i < 40000000; ++i) {
            String dni = i + "E\n";
            bw.write(dni);
        }
        bw.close();
    }
}