package sophena.rcp.editors.biogas.plant;

import java.util.Objects;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.Boiler;
import sophena.model.ProductCosts;
import sophena.model.biogas.BiogasPlant;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.editors.ProductCostSection;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class BiogasPlantInfoPage extends FormPage {

	private final BiogasPlantEditor editor;

	BiogasPlantInfoPage(BiogasPlantEditor editor) {
		super(editor, "BiogasPlantPage", "Biogasanlage");
		this.editor = editor;
	}

	private BiogasPlant plant() {
		return editor.plant();
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		var form = UI.formHeader(mForm, plant().name);
		var tk = mForm.getToolkit();
		var body = UI.formBody(form, tk);

		var comp = UI.formSection(body, tk, "BHKW");
		UI.gridLayout(comp, 3);

		// name
		Texts.on(UI.formText(comp, tk, M.Name))
				.required()
				.init(plant().name)
				.onChanged(s -> {
					plant().name = s;
					editor.setDirty();
				});
		UI.filler(comp, tk);

		// product group
		Controls.renderGroupLink(plant().productGroup, tk, comp);
		UI.filler(comp, tk);

		// description
		Texts.on(UI.formMultiText(comp, tk, M.Description))
				.init(plant().description)
				.onChanged(s -> {
					plant().description = s;
					editor.setDirty();
				});
		UI.filler(comp, tk);

		Texts.on(UI.formText(comp, tk, "Bemessungsleistung"))
				.init(plant().ratedPower)
				.integer()
				.onChanged(s -> {
					plant().ratedPower = Num.readInt(s);
					editor.setDirty();
				});
		UI.formLabel(comp, tk, "kW el.");

		Texts.on(UI.formText(comp, tk, "Laufzeit (Jahre)"))
				.init(plant().duration)
				.integer()
				.onChanged(s -> {
					plant().duration = Num.readInt(s);
					editor.setDirty();
				});
		UI.filler(comp, tk);

		// product section
		var productComp = UI.formSection(body, tk, "Produkt");
		UI.gridLayout(productComp, 3);
		var costs = new ProductCostSection(() -> plant().costs)
				.withEditor(editor);
		boilerCombo(tk, productComp, costs);
		costs.createFields(productComp, tk);

		// substrate section
		var substrateSection = SubstrateSection.of(editor);
		substrateSection.create(body, tk);

		// electricity section
		ElectricitySection.of(editor).create(body, tk);
		editor.calculate();
	}

	private void boilerCombo(
			FormToolkit tk, Composite comp, ProductCostSection costs
	) {
		var combo = new EntityCombo<Boiler>();
		combo.create("Produkt", comp, tk);
		combo.setLabelProvider(bi -> bi.name + " ("
				+ Num.str(bi.maxPowerElectric) + " kW el.)");
		var b = plant().product;
		if (b == null || b.group == null)
			return;
		var boilers = App.getDb().getAll(Boiler.class)
				.stream()
				.filter(bi -> Objects.equals(bi.group, b.group))
				.sorted(Sorters.byName())
				.toList();
		combo.setInput(boilers);
		combo.select(b);
		combo.onSelect(bi -> {
			plant().product = bi;
			ProductCosts.copy(bi, plant().costs);
			costs.refresh();
			editor.setDirty();
			editor.calculate();
		});
		UI.filler(comp, tk);
	}

}
