package sophena.rcp.colors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.slf4j.LoggerFactory;

import sophena.Labels;
import sophena.model.ProductType;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.UI;

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
		return new Point(1000, 600);
	}

	@Override
	protected void createFormContent(IManagedForm form) {
		var tk = form.getToolkit();
		var body = UI.formBody(form.getForm(), tk);

		keyGroupOf("Allgemeine Farben", body, tk,
				ColorKey.BUFFER_TANK,
				ColorKey.LOAD_DYNAMIC,
				ColorKey.LOAD_STATIC,
				ColorKey.PRODUCER_PROFILE,
				ColorKey.UNCOVERED_LOAD);

		keyGroupOf("Wärmenutzung", body, tk,
				ColorKey.USED_HEAT,
				ColorKey.PRODUCED_ELECTRICITY,
				ColorKey.LOSSES_BUFFER,
				ColorKey.LOSSES_DISTRIBUTION);

		keyGroupOf("Emissionen", body, tk,
				ColorKey.EMISSIONS,
				ColorKey.EMISSIONS_OIL,
				ColorKey.EMISSIONS_GAS);

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

	private void keyGroupOf(
			String label, Composite body, FormToolkit tk, ColorKey... keys
	) {
		var g = groupWidgetOf(label, body, tk);
		for (var key : keys) {
			ColorBox.of(labelOf(key), g, tk)
					.setColor(config.get(key))
					.onChange(rgb -> config.put(key, rgb));
		}
	}

	private String labelOf(ColorKey key) {
		if (key == null)
			return "unknown";
		return switch (key) {
			case BUFFER_TANK -> "Pufferspeicher";
			case LOAD_DYNAMIC -> "Dynamische Last";
			case LOAD_STATIC -> "Statische Last";
			case PRODUCER_PROFILE -> "Erzeugerprofil";
			case UNCOVERED_LOAD -> "Ungedeckte Leistung";

			case EMISSIONS -> "Wärmenetz";
			case EMISSIONS_GAS -> "Erdgas";
			case EMISSIONS_OIL -> "Heizöl";

			case USED_HEAT -> "Genutzte Wärme";
			case PRODUCED_ELECTRICITY -> "Erzeugter Strom";
			case LOSSES_BUFFER -> "Pufferspeicherverluste";
			case LOSSES_DISTRIBUTION -> "Verteilungsverluste";
		};
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
	protected void createButtonsForButtonBar(Composite comp) {
		createButton(comp, IDialogConstants.OK_ID, "Speichern", false);
		createButton(comp, 42, "Zurücksetzen", false);
		createButton(comp, 43, "Importieren", false);
		createButton(comp, 44, "Exportieren", false);
		createButton(comp, IDialogConstants.CANCEL_ID, "Abbrechen", true);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
			case 42 -> onReset();
			case 43 -> onImport();
			case 44 -> onExport();
			default -> super.buttonPressed(buttonId);
		}
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

	private void onReset() {
		var b = MsgBox.ask("Zurücksetzen?",
				"Sollen die Farben auf die Voreinstellungen zurückgesetzt werden?");
		if (!b)
			return;
		ColorConfig.reset();
		this.close();
		ColorConfigDialog.show();
	}

	private void onImport() {
		var file = FileChooser.open("*.json");
		if (file == null)
			return;
		ColorConfig config = null;
		try {
			config = ColorConfig.read(file);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("failed to read color configuration", e);
		}

		if (config == null)
			return;
		var b = MsgBox.ask("Importieren?",
				"Soll die Farbkonfiguration mit der ausgewählten " +
						"Datei überschrieben werden?");
		if (!b)
			return;
		ColorConfig.save(config);
		this.close();
		ColorConfigDialog.show();
	}

	private void onExport() {
		var file = FileChooser.save("sophena_colors.json", "*.json");
		if (file == null)
			return;
		ColorConfig.write(config, file);
	}
}
