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

import sophena.calc.biogas.BiogasRuntimeResult;
import sophena.calc.biogas.BiogasPlants;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

/// A simple result page of a biogas plant. It shows the most important figures
/// of the calculation. More details will be added later.
class BiogasPlantResultPage extends FormPage {

	private final BiogasPlantResultEditor editor;

	BiogasPlantResultPage(BiogasPlantResultEditor editor) {
		super(editor, "sophena.BiogasPlantResultPage", "Ergebnisse");
		this.editor = editor;
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		var result = editor.result();
		var form = UI.formHeader(mForm, "Ergebnisse: " + plantName(result));
		var tk = mForm.getToolkit();
		var body = UI.formBody(form, tk);

		createOverview(body, tk, result);

		form.reflow(true);
	}

	private void createOverview(
		Composite body, FormToolkit tk, BiogasRuntimeResult result
	) {
		var plant = result.plant();
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
		if (flags == null)
			return 0;
		int n = 0;
		for (boolean flag : flags) {
			if (flag)
				n++;
		}
		return n;
	}

	private static String plantName(BiogasRuntimeResult result) {
		var plant = result.plant();
		return plant == null || plant.name == null
			? "Biogasanlage"
			: plant.name;
	}

	private record Item(String label, String value) {
	}

	private static class ItemLabel extends LabelProvider
		implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (!(element instanceof Item item))
				return null;
			return columnIndex == 0 ? item.label() : item.value();
		}
	}
}
