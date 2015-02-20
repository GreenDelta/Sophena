package sophena.rcp.utils;

import java.util.HashMap;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cache {

	private static final HashMap<String, Object> map = new HashMap<>();

	public static String put(Object obj) {
		if (obj == null)
			return null;
		String key = UUID.randomUUID().toString();
		map.put(key, obj);
		return key;
	}

	@SuppressWarnings("unchecked")
	public static <T> T remove(String key) {
		try {
			Object obj = map.remove(key);
			return (T) obj;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Cache.class);
			log.error("failed to get " + key + " from cache", e);
			return null;
		}
	}

}
