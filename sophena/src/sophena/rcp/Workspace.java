package sophena.rcp;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.function.Supplier;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.PlatformUI;
import org.slf4j.LoggerFactory;

import sophena.rcp.utils.UI;

public class Workspace {

	private static File dir;

	/** Returns the workspace folder. */
	public static File dir() {
		return dir;
	}

	static void switchWorkspace() {
		switchWorkspace(null);
	}

	/**
	 * Returns the URL for the given HTML page in the workspace. If the
	 * corresponding file does not exist it is created from the given stream.
	 */
	public static String html(String page, Supplier<InputStream> stream) {
		var name = page + App.version() + ".html";
		var file = new File(Workspace.dir(), name);
		try {
			if (!file.exists()) {
				Files.copy(stream.get(), file.toPath());
			}
			return file.toURI().toURL().toString();
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(Workspace.class);
			log.error("Could not get URL to page {}", page, e);
			return "";
		}
	}

	static void switchWorkspace(String toDir) {
		AppConfig conf = AppConfig.load();
		if (toDir == null) {
			DirectoryDialog dialog = new DirectoryDialog(UI.shell());
			if (conf.dataDir != null) {
				dialog.setFilterPath(conf.dataDir);
			}
			toDir = dialog.open();
		}
		if (toDir == null || toDir.equals(conf.dataDir))
			return;
		conf.switchDataDir(toDir);
		PlatformUI.getWorkbench().restart();
	}

	static void init() {
		try {
			dir = getDataDir();
			Platform.getInstanceLocation().release();
			URL workspaceUrl = new URL("file", null, dir().getAbsolutePath());
			Platform.getInstanceLocation().set(workspaceUrl, true);
		} catch (Exception e) {
			// no logging here as the logger is not yet configured
			e.printStackTrace();
			if (dir == null) {
				dir = new File(".");
			}
		}
	}

	private static File getDataDir() {
		AppConfig conf = AppConfig.load();
		if (conf.dataDir != null) {
			File dir = new File(conf.dataDir);
			if (dir.exists())
				return dir;
		}
		File dir = new File("sophena_data");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		conf.dataDir = dir.getAbsolutePath();
		conf.save();
		return dir;
	}

}
