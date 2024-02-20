package database;

import java.io.BufferedReader;
import java.io.FileReader;
import org.iq80.leveldb.*;
import static org.iq80.leveldb.impl.Iq80DBFactory.*;
import java.io.*;

public class Database {
    DB db;

    public Database() throws IOException {
        this.openDatabase();
    }

    public void loadData(File publicKeyFile) throws IOException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(publicKeyFile));
            String key;
            while ((key = br.readLine()) != null) {
                this.changeValue(key, "0");
            }

            //check correct insert
            System.out.println(getValue("99185536K"));
            System.out.println(getValue("99744054Y"));
        } finally {
            this.closeDatabase();
        }
    }

    public void openDatabase() throws IOException {
        Options options = new Options();
        options.createIfMissing(true);
        this.db = factory.open(new File("localDatabase"), options);
    }

    public void closeDatabase() throws IOException {
        this.db.close();
    }

    public void changeValue(String key, String value) {
        this.db.put(bytes(key), bytes(value));
    }

    public String getValue(String key) {
        return asString(this.db.get(bytes(key)));
    }

    public static void main(String[] args) {

    }
}
