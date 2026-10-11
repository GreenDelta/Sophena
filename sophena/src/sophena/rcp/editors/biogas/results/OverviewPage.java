package sophena.rcp.editors.biogas.results;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.calc.biogas.runtime.BiogasRuntimeResult;
import sophena.calc.biogas.BiogasPlants;
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
		var table = createTable(UI.formSection(body, tk, "Übersicht"));

		var items = new ArrayList<Item>();
		items.add(new Item("Laufzeit", Num.intStr(plant.duration) + " Jahre"));
		items.add(new Item("Gasspeichergröße",
			Num.intStr(result.gasStorageSize()) + " m³"));
		items.add(new Item("Betriebsstunden",
			Num.intStr(runHours(result)) + " h/a"));
		items.add(new Item("Thermische Leistung",
			Num.intStr(BiogasPlants.totalThermalPower(plant)) + " kW"));
		items.add(new Item("Elektrische Leistung",
			Num.intStr(BiogasPlants.totalElectricPower(plant)) + " kW"));
		items.add(new Item("Brennstoffleistung",
			Num.intStr(BiogasPlants.fullLoadFuelPower(plant)) + " kW"));
		table.setInput(items);
	}

	private TableViewer createTable(Composite parent) {
		var table = Tables.createViewer(parent, "Position", "Wert");
		Tables.bindColumnWidths(table, 0.6, 0.4);
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

	private record Item(String label, String value) {
	}

	private static class ItemLabel extends LabelProvider
		implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof Item(String label, String value)))
				return null;
			return col == 0 ? label : value;
		}
	}
}
