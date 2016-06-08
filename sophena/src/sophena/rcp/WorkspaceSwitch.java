package sophena.rcp;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.PlatformUI;

import sophena.rcp.utils.UI;

class WorkspaceSwitch {

	static void run() {
		AppConfig conf = AppConfig.load();
		DirectoryDialog dialog = new DirectoryDialog(UI.shell());
		// TODO: dialog.setFilterPath("..."); -> propose current path
		String newDir = dialog.open();
		if (newDir == null) // or newDir = oldDir
			return;
		conf.dataDir = newDir;
		conf.save();
		PlatformUI.getWorkbench().restart();
	}

}
