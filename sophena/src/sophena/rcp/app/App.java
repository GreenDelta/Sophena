package sophena.rcp.app;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.Database;
import sophena.rcp.utils.MsgBox;

public class App {

	private static Database db;
	private static final HashMap<String, Object> cache = new HashMap<>();
	private static final EventBus events = new EventBus();

	private App() {
	}

	public static String version() {
		var context = Activator.getContext();
		if (context == null || context.getBundle() == null)
			return "2";
		var v = context.getBundle().getVersion();
		if (v == null)
			return "2";
		String s = v.toString();
		return s.endsWith(".0")
			? s.substring(0, s.length() - 2)
			: s;
	}

	public static void init(Database db) {
		App.db = db;
	}

	public static Database getDb() {
		return db;
	}

	public static EventBus events() {
		return events;
	}

	/**
	 * Used for temporary caching objects for the communication between editors as
	 * editor inputs should be lightweight. The returned key can be used to retrieve
	 * the object later (see {@link #pop(String)}).
	 */
	public static String stash(Object obj) {
		if (obj == null)
			return null;
		String key = UUID.randomUUID().toString();
		cache.put(key, obj);
		return key;
	}

	/**
	 * Same as {@link #stash(Object)} but with an explicit key.
	 */
	public static void stash(String key, Object value) {
		if (key == null)
			return;
		if (value == null) {
			cache.remove(key);
			return;
		}
		cache.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public static <T> T pop(String key) {
		if (key == null)
			return null;
		try {
			Object obj = cache.remove(key);
			return (T) obj;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(App.class);
			log.error("failed to get {} from cache", key, e);
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

	public static void runWithProgress(String name, Runnable runnable) {
		try {
			var progress = PlatformUI.getWorkbench().getProgressService();
			progress.run(true, false, (monitor) -> {
				monitor.beginTask(name, IProgressMonitor.UNKNOWN);
				runnable.run();
				monitor.done();
			});
		} catch (Exception e) {
			MsgBox.error("Unexpected error",
				"Error while running progress: " + name + " : " + e);
		}
	}

	/**
	 * Shows a progress indicator while running the given function in a separate
	 * thread. The calling thread is blocked while the given function is
	 * executed. It returns the result of the given function or `null` when
	 * calling that function failed.
	 */
	public static <T> T exec(String task, Supplier<T> fn) {
		var ref = new AtomicReference<T>();
		try {
			PlatformUI.getWorkbench().getProgressService()
				.busyCursorWhile((monitor) -> {
					monitor.beginTask(task, IProgressMonitor.UNKNOWN);
					ref.set(fn.get());
					monitor.done();
				});
		} catch (Exception e) {
			MsgBox.error("Unexpected error",
				"Error while running progress: " + task + " : " + e);
		}
		return ref.get();
	}

}
