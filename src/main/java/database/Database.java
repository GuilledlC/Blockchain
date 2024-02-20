package database;

import org.iq80.leveldb.*;
import static org.iq80.leveldb.impl.Iq80DBFactory.*;
import java.io.*;

public class Database {

    public void openDatabase() throws IOException {
        Options options = new Options();
        options.createIfMissing(true);
        DB db = factory.open(new File("localDatabase"), options);
        try {
            // Use the db in here....
            db.put(bytes("Tampa"), bytes("0"));
            String value = asString(db.get(bytes("Tampa")));
            System.out.println(value);
        } finally {
            // Make sure you close the db to shut down the
            // database and avoid resource leaks.
            db.close();
        }
    }

    public static void main(String[] args) {

    }
}
