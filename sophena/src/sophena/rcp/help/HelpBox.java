package sophena.rcp.help;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import sophena.rcp.Icon;
import sophena.rcp.utils.UI;

public class HelpBox extends Dialog {

	private final String title;
	private final String text;

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
		return new Point(600, 400);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
		shell.setImage(Icon.INFO_16.img());
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}
