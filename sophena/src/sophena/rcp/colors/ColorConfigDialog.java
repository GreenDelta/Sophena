package sophena.rcp.colors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import sophena.rcp.utils.UI;

public class ColorConfigDialog extends FormDialog {

	private ColorConfigDialog() {
		super(UI.shell());
	}

	public static void show() {
		new ColorConfigDialog().open();
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Ergebnisfarben anpassen");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 500);
	}

	@Override
	protected void createFormContent(IManagedForm form) {
		var tk = form.getToolkit();
		var body = UI.formBody(form.getForm(), tk);

		var group = new Group(body, SWT.NONE);
		group.setText("Test");
		UI.fillHorizontal(group);
	}
}
