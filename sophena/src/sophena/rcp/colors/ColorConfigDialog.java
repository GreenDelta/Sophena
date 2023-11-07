package sophena.rcp.colors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import sophena.Labels;
import sophena.model.ProductType;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.UI;

import java.util.ArrayList;
import java.util.List;

public class ColorConfigDialog extends FormDialog {

	private final ColorConfig config;

	private ColorConfigDialog() {
		super(UI.shell());
		this.config = ColorConfig.get().copy();
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
		return new Point(650, 600);
	}

	@Override
	protected void createFormContent(IManagedForm form) {
		var tk = form.getToolkit();
		var body = UI.formBody(form.getForm(), tk);

		var g = groupWidgetOf("Allgemeine Farben", body, tk);
		ColorBox.of("Pufferspeicher", g, tk)
				.setColor(config.forBufferTank())
				.onChange(config::setForBufferTank);

		// create groups
		var types = List.of(
				ProductType.BIOMASS_BOILER,
				ProductType.FOSSIL_FUEL_BOILER,
				ProductType.COGENERATION_PLANT,
				ProductType.ELECTRIC_HEAT_GENERATOR,
				ProductType.HEAT_PUMP,
				ProductType.SOLAR_THERMAL_PLANT,
				ProductType.OTHER_HEAT_SOURCE);
		for (var type : types) {
			var group = config.groupOf(type);
			renderGroup(group, body, tk);
		}
	}

	private void renderGroup(ColorGroup group, Composite parent, FormToolkit tk) {
		var g = groupWidgetOf(Labels.getPlural(group.type()), parent, tk);
		var variants = new ArrayList<ColorBox>();
		ColorBox.of("Grundfarbe", g, tk)
				.setColor(group.base())
				.onChange(rgb -> {
					group.setBase(rgb);
					for (int i = 0; i < variants.size(); i++) {
						var next = next(rgb, i);
						group.setVariant(i, next);
						variants.get(i).setColor(next);
					}
				});

		for (int i = 0; i < 5; i++) {
			int idx = i;
			var box = ColorBox.of("Variante " + (i + 1), g, tk)
					.setColor(group.variant(i))
					.onChange(rgb -> group.setVariant(idx, rgb));
			variants.add(box);
		}
	}

	private Group groupWidgetOf(String label, Composite parent, FormToolkit tk) {
		var g = new Group(parent, SWT.NONE);
		tk.adapt(g);
		g.setText(label);
		UI.fillHorizontal(g);
		UI.gridLayout(g, 6, 5, 2).makeColumnsEqualWidth = true;
		return g;
	}

	private RGB next(RGB origin, int i) {
		var hsb = origin.getHSB();
		float h = hsb[0];
		float s = hsb[1];
		float b = hsb[2];

		return switch (i) {
			case 0 -> new RGB(h, 0.65f, 0.65f);
			case 1 -> new RGB(h, 0.55f, 0.75f);
			case 2 -> new RGB(h, 0.45f, 0.85f);
			case 3 -> new RGB(h, 0.75f, 0.55f);
			case 4 -> new RGB(h, 0.85f, 0.45f);
			default -> {
				float nextH = (float) (h + Math.random() * 360) % 360;
				yield new RGB(nextH, s, b);
			}
		};
	}

	@Override
	protected void okPressed() {
		var origin = ColorConfig.get();
		if (origin.equals(config)) {
			super.okPressed();
			return;
		}
		var b = MsgBox.ask("Änderungen speichern",
				"Sollen die Änderungen gespeichert werden?");
		if (b) {
			ColorConfig.save(config);
		}
		super.okPressed();
	}
}
