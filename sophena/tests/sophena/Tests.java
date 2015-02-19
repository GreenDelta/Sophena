package sophena;

import java.io.File;

import sophena.db.Database;

public class Tests {

	private static Database db;

	public static Database getDb() {
		if (db != null)
			return db;
		String tempPath = System.getProperty("java.io.tmpdir");
		File tempDir = new File(tempPath);
		File dbDir = new File(tempDir, "sophena-test-db");
		System.out.println("using db-dir: " + dbDir);
		db = new Database(dbDir);
		return db;
	}

}
