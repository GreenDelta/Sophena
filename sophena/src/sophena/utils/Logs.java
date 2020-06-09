package sophena.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Logs {

	private Logs() {
	}

	public static void error(Class<?> clazz, String message, Object... info) {
		Logger log = LoggerFactory.getLogger(clazz);
		log.error(message, info);
	}

	public static void trace(Class<?> clazz, String message, Object... info) {
		Logger log = LoggerFactory.getLogger(clazz);
		log.trace(message, info);
	}
}
