package sophena.rcp.colors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import sophena.Labels;
import sophena.model.ProductType;
import sophena.rcp.colors.ColorConfig.Group;
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
		var config = ColorConfig.get().copy();

		// create groups
		var types = List.of(
				ProductType.BIOMASS_BOILER,
				ProductType.FOSSIL_FUEL_BOILER,
				ProductType.COGENERATION_PLANT,
				ProductType.ELECTRIC_HEAT_GENERATOR,
				ProductType.HEAT_PUMP,
				ProductType.SOLAR_THERMAL_PLANT,
				ProductType.OTHER_HEAT_SOURCE,
				ProductType.BUFFER_TANK);
		for (var type : types) {
			var group = config.groupOf(type);
			render(group, body, tk);
		}
	}

	private void render(Group group, Composite parent, FormToolkit tk) {
		var g = new org.eclipse.swt.widgets.Group(parent, SWT.NONE);
		tk.adapt(g);
		g.setText(Labels.getPlural(group.type()));
		UI.fillHorizontal(g);
		UI.gridLayout(g, 6).makeColumnsEqualWidth = true;
		ColorBox.of("Grundfarbe", g)
				.setColor(group.base())
				.onChange(group::setBase);
		for (int i = 0; i < 5; i++) {
			int idx = i;
			ColorBox.of("Variante " + (i + 1), g)
					.setColor(group.variant(i))
					.onChange(rgb -> group.setVariant(idx, rgb));
		}
	}

}
