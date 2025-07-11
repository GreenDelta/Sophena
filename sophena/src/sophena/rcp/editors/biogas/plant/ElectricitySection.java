package sophena.rcp.editors.biogas.plant;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.ElectricityPriceCurve;
import sophena.rcp.App;
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
		var root = UI.sectionClient(section, tk);
		UI.gridLayout(root, 1);

		var comboComp = tk.createComposite(root);
		UI.innerGrid(comboComp, 2).horizontalSpacing = 10;
		var combo = new EntityCombo<ElectricityPriceCurve>()
				.create("Strompreise", comboComp, tk)
				.setInput(App.getDb().getAll(ElectricityPriceCurve.class))
				.select(plant().electricityPrices);

		var chart = new ElectricityChart(root, 250);
		chart.setInput(plant());
		combo.onSelect(c -> {
			plant().electricityPrices = c;
			chart.setInput(plant());
			editor.setDirty();
		});
	}
}
