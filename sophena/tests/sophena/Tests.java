package sophena;

import java.io.File;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import sophena.db.Database;

public class Tests {

	private static Database db;

	static {
		Logger log = Logger.getRootLogger();
		log.setLevel(Level.ALL);
		log.addAppender(new ConsoleAppender(new PatternLayout(
				"%-4r %-5p %c %x - %m%n")));
	}

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
