package sophena.rcp.colors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import sophena.model.ProductType;
import sophena.rcp.utils.UI;

import java.util.List;

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

		var types = List.of(
				ProductType.BIOMASS_BOILER,
				ProductType.FOSSIL_FUEL_BOILER,
				ProductType.COGENERATION_PLANT,
				ProductType.ELECTRIC_HEAT_GENERATOR,
				ProductType.HEAT_PUMP,
				ProductType.SOLAR_THERMAL_PLANT,
				ProductType.OTHER_HEAT_SOURCE,
				ProductType.BUFFER_TANK);

		var group = new Group(body, SWT.NONE);
		tk.adapt(group);
		group.setText("Test");
		UI.fillHorizontal(group);

	}
}
