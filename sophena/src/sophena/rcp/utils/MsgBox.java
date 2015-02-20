package sophena.rcp.utils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;

//@rcp-template
public class MsgBox {

	private static final int ERROR = 0;
	private static final int WARNING = 1;
	private static final int INFO = 2;

	public static boolean ask(String title, String question) {
		return MessageDialog.openQuestion(UI.shell(), title, question);
	}

	public static void error(String message) {
		show(message, ERROR);
	}

	public static void error(String title, String message) {
		show(title, message, ERROR);
	}

	public static void warn(String message) {
		show(message, WARNING);
	}

	public static void warn(String title, String message) {
		show(title, message, WARNING);
	}

	public static void info(String message) {
		show(message, INFO);
	}

	public static void info(String title, String message) {
		show(title, message, INFO);
	}

	private static void show(String message, int type) {
		String title = null;
		switch (type) {
		case ERROR:
			title = "#Fehler";
			break;
		case WARNING:
			title = "#Warnung";
			break;
		case INFO:
			title = "#Information";
			break;
		default:
			break;
		}
		show(title, message, type);
	}

	private static void show(String title, String message, int type) {
		new BoxJob(title, message, type).schedule();
	}

	private static class BoxJob extends UIJob {

		private String title;
		private String message;
		private int type;

		public BoxJob(String title, String message, int type) {
			super("Open message box");
			this.title = title;
			this.message = message;
			this.type = type;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			Display display = getDisplay();
			if (display == null)
				return Status.CANCEL_STATUS;
			Shell shell = display.getActiveShell();
			if (shell == null)
				shell = new Shell(display);
			openBox(shell);
			return Status.OK_STATUS;
		}

		private void openBox(Shell shell) {
			switch (type) {
			case ERROR:
				MessageDialog.openError(shell, title, message);
				break;
			case WARNING:
				MessageDialog.openWarning(shell, title, message);
				break;
			case INFO:
				MessageDialog.openInformation(shell, title, message);
				break;
			default:
				break;
			}
		}
	}

}
