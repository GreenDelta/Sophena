package sophena.rcp.help;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import sophena.rcp.Icon;
import sophena.rcp.utils.UI;

public class HelpBox extends Dialog {

	private String title;
	private String text;

	private HelpBox(String title, String text) {
		super(UI.shell());
		this.title = title;
		this.text = text;
	}

	public static void show(String title, String helpText) {
		HelpBox box = new HelpBox(title, helpText);
		box.open();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		var comp = (Composite) super.createDialogArea(parent);
		var shell = getShell();
		if (shell != null && title != null) {
			shell.setText(title);
			shell.setImage(Icon.INFO_16.img());
		}
		comp.setLayout(new FillLayout());
		var browser = new Browser(comp, SWT.NONE);
		browser.setText(
				"<html><p style='font-family: Sans-Serif'>"
						+ text
						+ "</p></html>");
		return comp;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}
}
