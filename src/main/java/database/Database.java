package database;

import java.io.BufferedReader;
import java.io.FileReader;
import org.iq80.leveldb.*;
import static org.iq80.leveldb.impl.Iq80DBFactory.*;
import java.io.*;

public class Database {
    private DB db;

    public Database(String path) throws IOException {
        this.openDatabase(path);
    }

    public void loadData(File publicKeyFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(publicKeyFile));
        String key;
        while ((key = br.readLine()) != null) {
            this.changeValue(key, "0");
        }
    }

    public void openDatabase(String path) throws IOException {
        Options options = new Options();
        options.createIfMissing(true);
        this.db = factory.open(new File(path), options);
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
}
