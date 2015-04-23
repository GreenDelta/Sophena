package sophena.rcp.utils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.ui.dialogs.AbstractNotificationPopup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.progress.UIJob;

import sophena.rcp.Images;

public class Popup {

	private String message;
	private String title;
	private Images imageType;

	public static void showInfo(String message) {
		showInfo("Information", message);
	}

	public static void showInfo(String title, String message) {
		Popup p = new Popup(title, message);
		p.setImageType(Images.INFO_16);
		p.show();
	}

	public static void showWarning(String message) {
		showWarning("Warnung", message);
	}

	public static void showWarning(String title, String message) {
		Popup p = new Popup(title, message);
		p.setImageType(Images.WARNING_16);
		p.show();
	}

	public static void showError(String message) {
		showError("Unerwarteter Fehler", message);
	}

	public static void showError(String title, String message) {
		Popup p = new Popup(title, message);
		p.setImageType(Images.ERROR_16);
		p.show();
	}

	public Popup(String title, String message) {
		this.title = title;
		this.message = message;
	}

	public void setImageType(Images imageType) {
		this.imageType = imageType;
	}

	public void show() {
		UIJob job = new UIJob("Open popup") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				Display display = getDisplay();
				if (display == null || display.isDisposed())
					return Status.CANCEL_STATUS;
				new PopupImpl(display).open();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	public class PopupImpl extends AbstractNotificationPopup {

		public PopupImpl(Display display) {
			super(display);
		}

		@Override
		protected String getPopupShellTitle() {
			return title;
		}

		@Override
		protected void initializeBounds() {
			// Georg: Workaround for a sizing bug in the extended implementation
			super.initializeBounds();
			Point currentSize = getShell().getSize();
			Point currentLoc = getShell().getLocation();
			Point newSize = getShell().computeSize(400, SWT.DEFAULT);
			int widthDiff = newSize.x - currentSize.x;
			int heightDiff = newSize.y - currentSize.y;
			Point newLoc = new Point(currentLoc.x - widthDiff, //
					currentLoc.y - heightDiff);
			getShell().setLocation(newLoc);
			getShell().setSize(newSize);
		}

		@Override
		protected Image getPopupShellImage(int maximumHeight) {
			if (imageType == null)
				return Images.INFO_16.img();
			else
				return imageType.img();
		}

		@Override
		protected void createContentArea(Composite composite) {
			composite.setLayout(new GridLayout(1, true));
			Label label = new Label(composite, SWT.WRAP);
			label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			label.setText(Strings.cut(message, 500));
			label.setBackground(composite.getBackground());
		}
	}
}
