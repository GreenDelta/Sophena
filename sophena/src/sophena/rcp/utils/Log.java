package sophena.rcp.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {

	private Log() {
	}

	public static void error(Object caller, String message, Throwable error) {
		Class<?> c = caller != null ? caller.getClass() : Log.class;
		Logger log = LoggerFactory.getLogger(c);
		log.error(message, error);
	}

}
