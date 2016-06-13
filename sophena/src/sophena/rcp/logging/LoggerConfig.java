package sophena.rcp.logging;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import sophena.rcp.utils.EclipseCommandLine;

/**
 * The configuration of the application logging.
 */
public class LoggerConfig {

	public static void setUp() {
		Logger logger = Logger.getRootLogger();
		setLogLevel(logger);
		HtmlLogFile.create(logger);
		logger.addAppender(new PopupAppender());
		addConsoleOutput(logger);
	}

	private static void addConsoleOutput(Logger logger) {
		BasicConfigurator.configure();
		ConsoleAppender appender = new ConsoleAppender(new PatternLayout());
		logger.addAppender(appender);
		appender.setTarget(ConsoleAppender.SYSTEM_OUT);
		appender.activateOptions();
	}

	private static void setLogLevel(Logger logger) {
		String level = EclipseCommandLine.getArg("logLevel");
		if (level != null) {
			setLevelFromCommandLine(logger, level);
		} else {
			logger.setLevel(Level.INFO);
		}
	}

	private static void setLevelFromCommandLine(Logger logger, String level) {
		if (level.equalsIgnoreCase("all")) {
			logger.setLevel(Level.ALL);
		} else if (level.equalsIgnoreCase("error")) {
			logger.setLevel(Level.ERROR);
		} else if (level.equalsIgnoreCase("info")) {
			logger.setLevel(Level.INFO);
		} else if (level.equalsIgnoreCase("warn")) {
			logger.setLevel(Level.WARN);
		} else {
			logger.setLevel(Level.INFO);
		}
	}
}
