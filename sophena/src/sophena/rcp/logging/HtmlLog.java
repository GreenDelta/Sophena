package sophena.rcp.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.html.HTMLLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import org.slf4j.LoggerFactory;
import sophena.rcp.Workspace;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * The configuration of the HTML log file.
 */
class HtmlLog {

	private HtmlLog() {
	}

	static RollingFileAppender<ILoggingEvent> create() {
		var factory = LoggerFactory.getILoggerFactory();
		if (!(factory instanceof LoggerContext context))
			return null;

		var logDir = new File(Workspace.dir(), "log");
		if (!logDir.exists()) {
			try {
				Files.createDirectories(logDir.toPath());
			} catch (IOException e) {
				var log = LoggerFactory.getLogger(HtmlLog.class);
				log.error("failed to create log-dir: " + logDir, e);
				return null;
			}
		}

		var appender = new RollingFileAppender<ILoggingEvent>();
		appender.setContext(context);
		appender.setName("html");

		var policy = new TimeBasedRollingPolicy<ILoggingEvent>();
		policy.setContext(context);
		policy.setFileNamePattern(
				logDir.getAbsolutePath() + "/log-%d{yyyy-MM-dd}.html");
		policy.setMaxHistory(3);
		policy.setCleanHistoryOnStart(true);
		policy.setTotalSizeCap(FileSize.valueOf("3MB"));
		policy.setParent(appender);
		policy.start();

		var layout = new Html5Layout();
		layout.setContext(context);
		layout.setPattern("%d{HH:mm:ss}%level%logger%msg");
		layout.start();

		var encoder = new LayoutWrappingEncoder<ILoggingEvent>();
		encoder.setContext(context);
		encoder.setCharset(StandardCharsets.UTF_8);
		encoder.setLayout(layout);
		encoder.start();

		appender.setRollingPolicy(policy);
		appender.setEncoder(encoder);
		appender.setAppend(true);
		appender.start();

		return appender;
	}

	public static class Html5Layout extends HTMLLayout {

		@Override
		public String getFileHeader() {
			var sep = CoreConstants.LINE_SEPARATOR;
			var buf = new StringBuilder()
					.append("<!DOCTYPE html")
					.append(sep)
					.append("<html>")
					.append(sep)
					.append("  <head>")
					.append(sep)
					.append("    <title>")
					.append(title)
					.append("</title>")
					.append(sep);

			cssBuilder.addCss(buf);

			buf.append(sep)
					.append("  </head>")
					.append(sep)
					.append("<body>")
					.append(sep);
			return buf.toString();
		}
	}
}
