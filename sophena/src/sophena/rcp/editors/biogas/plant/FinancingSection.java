package sophena.rcp.editors.biogas.plant;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.biogas.BiogasPlant;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class FinancingSection {

	private final BiogasPlantEditor editor;

	private FinancingSection(BiogasPlantEditor editor) {
		this.editor = editor;
	}

	static FinancingSection of(BiogasPlantEditor editor) {
		return new FinancingSection(editor);
	}

	private BiogasPlant plant() {
		return editor.plant();
	}

	void create(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk, "Finanzierung");
		UI.gridLayout(comp, 4);
		var settings = plant().settings;

		t(comp, tk, "Kapital-Mischzinssatz", "%", settings.interestRate)
			.onChanged(s -> settings.interestRate = Num.read(s));
		UI.filler(comp, tk);

		t(comp, tk, "Investitionsförderung", "EUR", settings.funding)
			.onChanged(s -> settings.funding = Num.read(s));
		UI.filler(comp, tk);

		t(comp, tk, "Gesamtkapitalrentabilität", "%",
			settings.capitalReturnRate)
			.onChanged(s -> settings.capitalReturnRate = Num.read(s));
		UI.filler(comp, tk);

		t(comp, tk, "Jahresüberschuss", "EUR/a",
			settings.expectedAnnualSurplus)
			.onChanged(s -> settings.expectedAnnualSurplus = Num.read(s));
		UI.filler(comp, tk);
	}

	private Texts.TextBox t(
		Composite comp, FormToolkit tk, String label, String unit, double initial
	) {
		Text text = UI.formText(comp, tk, label);
		UI.formLabel(comp, tk, unit);
		return Texts.on(text)
			.decimal()
			.init(initial)
			.onChanged(_ -> editor.setDirty());
	}
}
