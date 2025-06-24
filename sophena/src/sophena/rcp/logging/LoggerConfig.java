package sophena.rcp.logging;


import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import sophena.rcp.utils.EclipseCommandLine;

/**
 * The configuration of the application logging.
 */
public class LoggerConfig {

	public static void setup() {
		var root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		if (!(root instanceof Logger log))
			return;
		log.addAppender(PopupAppender.create());
		log.addAppender(HtmlLog.create());
		var arg = EclipseCommandLine.getArg("logLevel");
		var level = arg != null
				? levelOf(arg)
				: Level.INFO;
		log.setLevel(level);
	}

	private static Level levelOf(String s) {
		if (s == null)
			return Level.INFO;
		return switch (s.trim().toLowerCase()) {
			case "all" -> Level.ALL;
			case "error" -> Level.ERROR;
			case "warn" -> Level.WARN;
			default -> Level.INFO;
		};
	}
}
