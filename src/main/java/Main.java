import database.Database;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            //File file = new File("./src/main/resources/publicKeys.txt");
            Database database = new Database();
            System.out.println(database.getValue("27384915E"));
            database.closeDatabase();
        } catch (IOException error){
            error.printStackTrace();
        }
    }
}