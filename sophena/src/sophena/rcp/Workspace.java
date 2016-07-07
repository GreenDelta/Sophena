package sophena.rcp;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.PlatformUI;

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
