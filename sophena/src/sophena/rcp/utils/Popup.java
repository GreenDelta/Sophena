package sophena.rcp.utils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.nebula.widgets.opal.notifier.Notifier;
import org.eclipse.nebula.widgets.opal.notifier.NotifierColorsFactory.NotifierTheme;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

import sophena.rcp.Icon;

public class Popup {

	private final String text;
	private final String title;
	private final Icon icon;

	private Popup(String title, String message, Icon icon) {
		this.title = title;
		this.text = message;
		this.icon = icon;
	}

	public static void info(String message) {
		info("Information", message);
	}

	public static void info(String title, String message) {
		Popup p = new Popup(title, message, Icon.INFO_16);
		p.show();
	}

	public static void warning(String message) {
		warning("Warnung", message);
	}

	public static void warning(String title, String message) {
		Popup p = new Popup(title, message, Icon.WARNING_16);
		p.show();
	}

	public static void error(String message) {
		error("Unerwarteter Fehler", message);
	}

	public static void error(String title, String message) {
		Popup p = new Popup(title, message, Icon.ERROR_16);
		p.show();
	}

	public void show() {
		UIJob job = new UIJob("Open popup") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				Display display = getDisplay();
				if (display == null || display.isDisposed())
					return Status.CANCEL_STATUS;

				Notifier.notify(
						icon.img(),
						title != null ? title : "?",
						text != null ? text : "?",
						NotifierTheme.YELLOW_THEME);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

}
