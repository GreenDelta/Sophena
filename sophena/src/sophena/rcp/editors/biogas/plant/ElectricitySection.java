package sophena.rcp.editors.biogas.plant;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.commons.Strings;

import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.ElectricityPriceCurve;
import sophena.rcp.app.App;
import sophena.rcp.editors.biogas.charts.ElectricityChart;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.UI;

class ElectricitySection {

	private final BiogasPlantEditor editor;

	private ElectricitySection(BiogasPlantEditor editor) {
		this.editor = editor;
	}

	static ElectricitySection of(BiogasPlantEditor editor) {
		return new ElectricitySection(editor);
	}

	private BiogasPlant plant() {
		return editor.plant();
	}

	void create(Composite body, FormToolkit tk) {
		var section = UI.section(body, tk, "Strompreise & Laufzeit");
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);

		var comboComp = tk.createComposite(comp);
		UI.innerGrid(comboComp, 2).horizontalSpacing = 10;
		var curves = App.getDb().getAll(ElectricityPriceCurve.class);
		curves.sort((ci, cj) -> Strings.compareNatural(cj.name, ci.name));

		var combo = new EntityCombo<ElectricityPriceCurve>()
				.create("Strompreise", comboComp, tk)
				.setInput(curves)
				.select(plant().electricityPrices);

		var chart = new ElectricityChart(comp);
		editor.onResult(r -> chart.setInput(plant(), r));
		combo.onSelect(c -> {
			plant().electricityPrices = c;
			editor.setDirty();
			editor.calculate();
		});
	}
}
