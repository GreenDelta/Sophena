package sophena.rcp.utils;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import sophena.rcp.M;

public class FileChooser {

	/**
	 * Selects a file for an export. Returns null if the user cancelled the
	 * dialog.
	 */
	public static File save(String fileName, String... extensions) {
		FileDialog dialog = new FileDialog(UI.shell(), SWT.SAVE);
		dialog.setText("Speichern unter...");
		if (extensions.length > 0)
			dialog.setFilterExtensions(extensions);
		dialog.setFileName(fileName);
		String path = dialog.open();
		if (path == null)
			return null;
		File file = new File(path);
		if (!file.exists())
			return file;
		boolean b = MsgBox.ask("Datei überschreiben?",
				"Die ausgewählte Datei existiert bereits. "
						+ "Soll diese überschrieben werden?");
		return b ? file : null;
	}

	public static File open(String... extensions) {
		FileDialog dialog = new FileDialog(UI.shell(), SWT.OPEN);
		dialog.setFilterExtensions(extensions);
		dialog.setText(M.SelectFile);
		String path = dialog.open();
		if (path == null)
			return null;
		File f = new File(path);
		if (!f.exists())
			return null;
		return f;
	}

}
