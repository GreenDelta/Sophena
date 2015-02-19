package sophena.rcp.utils;

import java.util.HashMap;
import java.util.UUID;

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
	public static <T> T get(String key) {
		try {
			Object obj = map.get(key);
			return (T) obj;
		} catch (Exception e) {
			e.printStackTrace(); // TODO: log
			return null;
		}
	}

}
