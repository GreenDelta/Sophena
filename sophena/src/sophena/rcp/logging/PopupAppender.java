package sophena.rcp.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sophena.rcp.utils.Popup;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * An appender for log-messages in the user interface as pop-ups.
 */
class PopupAppender extends AppenderBase<ILoggingEvent> {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final AtomicInteger openPopupCount = new AtomicInteger(0);
	private final AtomicInteger failureCounter = new AtomicInteger(0);

	private PopupAppender() {
	}

	static PopupAppender create() {
		var factory = LoggerFactory.getILoggerFactory();
		if (!(factory instanceof LoggerContext ctx))
			return null;
		var appender = new PopupAppender();
		appender.setContext(ctx);
		appender.setName("popup");
		appender.start();
		return appender;
	}

	@Override
	protected void append(ILoggingEvent event) {
		if (event == null
				|| !event.getLevel().isGreaterOrEqual(Level.ERROR)
				|| openPopupCount.get() >= 5
				|| !PlatformUI.isWorkbenchRunning())
			return;
		try {
			openPopupCount.incrementAndGet();
			new PopupTokenWatch().start();
			Popup.error(event.getMessage());
		} catch (Exception e) {
			handlePopupError(e);
		}
	}

	/**
	 * If the creation of the popup creates an error itself handle it here.
	 */
	private void handlePopupError(Exception e) {
		if (failureCounter.incrementAndGet() > 3) {
			log.warn("Showing of failed error popups "
					+ "stopped because of repetetive failures");
		} else {
			log.error("Show message failed", e);
		}
	}


	private class PopupTokenWatch extends Thread {
		@Override
		public void run() {
			try {
				int POPUP_REMOVAL_TIME = 5000;
				sleep(POPUP_REMOVAL_TIME);
				openPopupCount.decrementAndGet();
			} catch (Exception e) {
				log.warn("Failed to remove error pop-up token", e);
			}
		}
	}

}
