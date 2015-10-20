package sophena.rcp;

import java.util.HashMap;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.Database;

public class App {

	private static Database db;
	private static final HashMap<String, Object> cache = new HashMap<>();

	private App() {
	}

	public static void init(Database db) {
		App.db = db;
	}

	public static Database getDb() {
		return db;
	}

	/**
	 * Used for temporary caching objects for the communication between editors
	 * as editor inputs should be lightweight. The returned key can be used to
	 * retrieve the object later (see {@link #pop(String)}).
	 */
	public static String stash(Object obj) {
		if (obj == null)
			return null;
		String key = UUID.randomUUID().toString();
		cache.put(key, obj);
		return key;
	}

	@SuppressWarnings("unchecked")
	public static <T> T pop(String key) {
		if (key == null)
			return null;
		try {
			Object obj = cache.remove(key);
			return (T) obj;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(App.class);
			log.error("failed to get " + key + " from cache", e);
			return null;
		}
	}

}
