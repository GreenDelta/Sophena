package sophena.rcp.utils;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Rcp {

	private Rcp() {
	}

	/**
	 * Returns the workspace directory of the application.
	 */
	public static File getWorkspace() {
		try {
			URL url = Platform.getInstanceLocation().getURL();
			return new File(url.toURI());
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Rcp.class);
			log.error("failed to get workspace location", e);
			return null;
		}
	}

	public static void runInUI(String name, Runnable runnable) {
		WrappedUIJob job = new WrappedUIJob(name, runnable);
		job.setUser(true);
		job.schedule();
	}

	/**
	 * Wraps a runnable in a job and executes it using the Eclipse jobs
	 * framework. No UI access is allowed for the runnable.
	 */
	public static void run(String name, Runnable runnable) {
		run(name, runnable, null);
	}

	/**
	 * See {@link App#run(String, Runnable)}. Additionally, this method allows
	 * to give a callback which is executed in the UI thread when the runnable
	 * is finished.
	 */
	public static void run(String name, Runnable runnable, Runnable callback) {
		WrappedJob job = new WrappedJob(name, runnable);
		if (callback != null)
			job.setCallback(callback);
		job.setUser(true);
		job.schedule();
	}

}