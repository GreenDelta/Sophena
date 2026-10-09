package sophena.rcp.editors.biogas.plant;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.biogas.BiogasPlant;
import sophena.rcp.utils.Controls;
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

		t(comp, tk, "Investitionsförderung absolut", "EUR", settings.funding)
			.onChanged(s -> settings.funding = Num.read(s));
		UI.filler(comp, tk);

		createProfitExpectationRow(comp, tk);
	}

	private void createProfitExpectationRow(Composite comp, FormToolkit tk) {
		var settings = plant().settings;

		UI.formLabel(comp, tk, "Gewinnerwartung");
		var inner = tk.createComposite(comp);
		UI.innerGrid(inner, 3);
		UI.gridData(inner, true, false).horizontalSpan = 3;

		var returnRadio = tk.createButton(inner, "Renditeerwartung", SWT.RADIO);
		var returnBox = Texts.on(UI.formText(inner, tk, null))
			.decimal()
			.init(settings.rateOfReturn)
			.onChanged(s -> settings.rateOfReturn = Num.read(s))
			.onChanged(_ -> editor.setDirty());
		UI.formLabel(inner, tk, "%");

		var surplusRadio = tk.createButton(inner, "Jahresüberschuss", SWT.RADIO);
		var surplusBox = Texts.on(UI.formText(inner, tk, null))
			.decimal()
			.init(settings.expectedAnnualSurplus)
			.onChanged(s -> settings.expectedAnnualSurplus = Num.read(s))
			.onChanged(_ -> editor.setDirty());
		UI.formLabel(inner, tk, "EUR/a");

		Runnable updateState = () -> {
			if (returnRadio.getSelection()) {
				returnBox.enable();
				surplusBox.disable();
			} else {
				returnBox.disable();
				surplusBox.enable();
			}
		};

		returnRadio.setSelection(settings.useRateOfReturn);
		surplusRadio.setSelection(!settings.useRateOfReturn);
		updateState.run();

		Controls.onSelect(returnRadio, _ -> {
			settings.useRateOfReturn = true;
			updateState.run();
			editor.setDirty();
		});
		Controls.onSelect(surplusRadio, _ -> {
			settings.useRateOfReturn = false;
			updateState.run();
			editor.setDirty();
		});
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
