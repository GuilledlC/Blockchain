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
        // The value indicates the state of the user
        // 0 means the user is not on the vote pool, and it has not voted yet
        // 1 means the user is on the vote pool, and it has not voted yet
        // 2 means the user has voted
        this.db.put(bytes(key), bytes(value));
    }

    public boolean hasVoted(String key) {
        try {
            String value = new String(this.db.get(bytes(key)));
            return value.equals("2");
        } catch (NullPointerException e) {
            return true;
        }
    }

	public boolean notExists(String key) {
		try {
			String value = new String(this.db.get(bytes(key)));
			return value.equals("0");
		} catch (NullPointerException e) {
			return false;
		}
	}
}
