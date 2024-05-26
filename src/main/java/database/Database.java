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
            this.changeValue(key, false);
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

    public void changeValue(String key, boolean value) {
        this.db.put(bytes(key), new byte[]{(byte) (value ? 1 : 0)});
    }

    public boolean hasVoted(String key) {
        try {
            return this.db.get(bytes(key))[0] == 1;
        } catch (NullPointerException e) {
            return true;
        }
    }
}
