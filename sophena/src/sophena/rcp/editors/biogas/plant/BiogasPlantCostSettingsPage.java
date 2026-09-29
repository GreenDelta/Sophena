package sophena.rcp.editors.biogas.plant;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.biogas.BiogasPlant;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class BiogasPlantCostSettingsPage extends FormPage {

	private final BiogasPlantEditor editor;
	private FormToolkit tk;

	BiogasPlantCostSettingsPage(BiogasPlantEditor editor) {
		super(editor, "BiogasPlantCostSettingsPage", "Kosteneinstellungen");
		this.editor = editor;
	}

	private BiogasPlant plant() {
		return editor.plant();
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		var form = UI.formHeader(mForm, "Kosteneinstellungen - " + plant().name);
		tk = mForm.getToolkit();
		var body = UI.formBody(form, tk);
		createGeneralSection(body);
		createOtherCostsSection(body);
		BiogasAnnualCostsTable.of(editor).render(body, tk);
		createPriceChangeSection(body);

		form.reflow(true);
	}

	private void createGeneralSection(Composite body) {
		var comp = UI.formSection(body, tk, "Allgemein");
		UI.gridLayout(comp, 3);

		t(comp, "Kapital-Mischzinssatz", "%", plant().interestRate)
				.onChanged(s -> plant().interestRate = Num.read(s));
	}

	private void createOtherCostsSection(Composite body) {
		var comp = UI.formSection(body, tk, "Sonstige Kosten");
		UI.gridLayout(comp, 3);

		t(comp, "Versicherung", "%", plant().insuranceShare)
				.onChanged(s -> plant().insuranceShare = Num.read(s));
	}

	private void createPriceChangeSection(Composite body) {
		var comp = UI.formSection(body, tk, "Preisänderungsfaktoren");
		UI.gridLayout(comp, 3);

		t(comp, "Investitionen", "", plant().investmentFactor)
				.onChanged(s -> plant().investmentFactor = Num.read(s));

		t(comp, "Biomasse-Brennstoff", "", plant().bioFuelFactor)
				.onChanged(s -> plant().bioFuelFactor = Num.read(s));

		t(comp, "Fossiler Brennstoff", "", plant().fossilFuelFactor)
				.onChanged(s -> plant().fossilFuelFactor = Num.read(s));

		t(comp, "Strom", "", plant().electricityFactor)
				.onChanged(s -> plant().electricityFactor = Num.read(s));

		t(comp, "Lohnkosten und sonstige Kosten", "", plant().operationFactor)
				.onChanged(s -> plant().operationFactor = Num.read(s));

		t(comp, "Instandhaltung", "", plant().maintenanceFactor)
				.onChanged(s -> plant().maintenanceFactor = Num.read(s));

		t(comp, "Strommehrerlöse", "", plant().electricityRevenuesFactor)
				.onChanged(s -> plant().electricityRevenuesFactor = Num.read(s));
	}

	private Texts.TextBox t(Composite comp, String label, String unit, double initial) {
		Text text = UI.formText(comp, tk, label);
		UI.formLabel(comp, tk, unit);
		return Texts.on(text)
				.decimal()
				.init(initial)
			.onChanged(_ -> editor.setDirty());
	}
}
