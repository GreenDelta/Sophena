package sophena.rcp;

import sophena.db.Database;

public class App {

	private static Database db;

	private App() {
	}

	public static void init(Database db) {
		App.db = db;
	}

	public static Database getDb() {
		return db;
	}

}
