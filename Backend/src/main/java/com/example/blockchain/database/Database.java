package com.example.blockchain.database;

import org.iq80.leveldb.*;
import static org.iq80.leveldb.impl.Iq80DBFactory.*;
import java.io.*;

public class Database {
    private DB db;

    public Database(String path) throws IOException {
        this.openDatabase(path);
    }

    private void openDatabase(String path) throws IOException {
        Options options = new Options();
        options.createIfMissing(true);
        this.db = factory.open(new File(path), options);
    }

    public void closeDatabase() throws IOException {
        this.db.close();
    }

    public void putValue(byte[] key, State value) {
        // The value indicates the state of the user
        // 0 means the user is not on the vote pool, and it has not voted yet
        // 1 means the user is on the vote pool, and it has not voted yet
        // 2 means the user has voted
        this.db.put(key, bytes(value.getValue()));
    }

    public boolean hasVoted(byte[] key) throws NullPointerException {
		String value = new String(this.db.get(key));
		return value.equals("2");
    }

	public boolean exists(byte[] key) {
		try {
			String value = new String(this.db.get(key));
			return value.equals("0");
		} catch (NullPointerException e) {
			return false;
		}
	}

	public enum State {
		Exists("0"),
		InPool("1"),
		Voted("2");

		private final String value;

		State(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

	}
}
