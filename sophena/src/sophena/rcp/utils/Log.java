package sophena.rcp.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {

	private Log() {
	}

	public static void error(Object caller, String message, Throwable error) {
		Class<?> c;
		if (caller instanceof Class) {
			c = (Class<?>) caller;
		} else if (caller != null) {
			c = caller.getClass();
		} else {
			c = Log.class;
		}
		Logger log = LoggerFactory.getLogger(c);
		log.error(message, error);
	}

}
