package sophena.rcp.editors.biogas.results;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.calc.biogas.runtime.BiogasRuntimeResult;
import sophena.calc.biogas.BiogasPlants;
import sophena.rcp.colors.Colors;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

/// A simple result page of a biogas plant. It shows the most important figures
/// of the calculation. More details will be added later.
class OverviewPage extends FormPage {

	private final BiogasResultEditor editor;

	OverviewPage(BiogasResultEditor editor) {
		super(editor, "sophena.OverviewPage", "Ergebnisse");
		this.editor = editor;
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		var result = editor.result().runtime();
		var form = UI.formHeader(mForm,
			"Ergebnisse: " + editor.result().plant().name);
		var tk = mForm.getToolkit();
		var body = UI.formBody(form, tk);

		createOverview(body, tk, result);

		form.reflow(true);
	}

	private void createOverview(
		Composite body, FormToolkit tk, BiogasRuntimeResult result
	) {
		var plant = editor.result().plant();
		var table = createTable(UI.formSection(body, tk, "Biogas-Kennzahlen"));
		var revs = editor.result().revenues();

		var items = new ArrayList<Item>();

		items.add(
			Item.of("Betriebsviertelstunden bei < 0 ct/kWh",
				Num.intStr(revs.quarterHoursBelowZero()), "BVh/a"));
		items.add(
			Item.of("Betriebsviertelstunden bei ≤ 2ct/kWh",
				Num.intStr(revs.quarterHoursBelowTwo()), "BVh/a"));
		items.add(
			Item.of("Summe der Betriebsviertelstunden",
				Num.intStr(revs.totalQuarterHours()), "BVh/a"));

		if (!plant.settings.isFixedRemuneration) {
			var item = revs.totalQuarterHours() < plant.settings.eligibleQuarterHours
				? Item.of("Förderfähige Betriebsviertelstunden", "eingehalten", "")
				: Item.errorOf("Förderfähige Betriebsviertelstunden", "nicht eingehalten", "");
			items.add(item);
		}

		int above85 = (int) revs.quarterHoursAbove85();
		items.add(
			Item.of("Viertelstunden >= 85% Leistung",
				Num.intStr(above85), "Vh/a"));
		if (above85 >= 4000) {
			items.add(
				Item.of("Qualitätskriterium Flexiblitätsbetrieb",	"eingehalten", ""));
		} else {
			items.add(
				Item.errorOf("Qualitätskriterium Flexiblitätsbetrieb",
					"nicht eingehalten", ""));
		}

		items.add(
			Item.of("Jährliche erzeugte Wärmemenge",
				Num.intStr(revs.totalGeneratedHeat()), "kWh/a"));




		items.add(Item.of("Laufzeit", Num.intStr(plant.duration), "Jahre"));
		items.add(Item.of("Gasspeichergröße",
			Num.intStr(result.gasStorageSize()), "m³"));
		items.add(Item.of("Betriebsstunden",
			Num.intStr(runHours(result)), "h/a"));
		items.add(Item.of("Thermische Leistung",
			Num.intStr(BiogasPlants.totalThermalPower(plant)), "kW"));
		items.add(Item.of("Elektrische Leistung",
			Num.intStr(BiogasPlants.totalElectricPower(plant)), "kW"));
		table.setInput(items);
	}

	private TableViewer createTable(Composite parent) {
		var table = Tables.createViewer(parent, "Name", "Wert", "Einheit");
		Tables.bindColumnWidths(table, 0.5, 0.3, 0.2);
		Tables.rightAlignColumns(table, 1);
		table.setLabelProvider(new ItemLabel());
		return table;
	}

	private static int runHours(BiogasRuntimeResult result) {
		var flags = result.runFlags();
		int n = 0;
		for (boolean flag : flags) {
			if (flag)
				n++;
		}
		return n;
	}

	private record Item(
		String name, String value, String unit, boolean isError
	) {

		static Item of(String name, String value, String unit) {
			return new Item(name, value, unit, false);
		}

		static Item errorOf(String name, String value, String unit) {
			return new Item(name, value, unit, true);
		}
	}

	private static class ItemLabel extends LabelProvider
		implements ITableLabelProvider, ITableColorProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public Color getForeground(Object obj, int col) {
			if (col == 1 && obj instanceof Item item && item.isError())
				return Colors.getSystemColor(SWT.COLOR_RED);
			return null;
		}

		@Override
		public Color getBackground(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof Item item))
				return null;
			return switch (col) {
				case 0 -> item.name();
				case 1 -> item.value();
				case 2 -> item.unit();
				default -> null;
			};
		}
	}
}
