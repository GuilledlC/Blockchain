import database.Database;

import javax.xml.crypto.Data;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Database database = new Database();
        try {
            database.openDatabase();
        } catch (IOException hey){
            System.out.println("hey");
        }
    }
}