package sophena.rcp.editors.biogas.plant;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.calc.biogas.BiogasPlantResult;
import sophena.model.biogas.BiogasPlant;
import sophena.rcp.M;
import sophena.rcp.app.App;
import sophena.rcp.app.Icon;
import sophena.rcp.editors.biogas.results.BiogasPlantResultEditor;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpLink;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class BiogasPlantInfoPage extends FormPage {

	private final BiogasPlantEditor editor;

	BiogasPlantInfoPage(BiogasPlantEditor editor) {
		super(editor, "BiogasPlantPage", "Biogasanlageninformtionen");
		this.editor = editor;
	}

	private BiogasPlant plant() {
		return editor.plant();
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		var form = UI.formHeader(mForm, "Biogasanlageninformtionen");
		var tk = mForm.getToolkit();
		var body = UI.formBody(form, tk);

		createInfoSection(body, tk);
		GeneralSection.of(editor).create(body, tk);
		LossesSection.of(editor).create(body, tk);
		FinancingSection.of(editor).create(body, tk);

		createOtherCostsSection(body, tk);
		BiogasAnnualCostsTable.of(editor).render(body, tk);
		FundingSection.of(editor).create(body, tk);
		createPriceChangeSection(body, tk);

		editor.calculate();
	}

	private void createInfoSection(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk, "Biogasanlage");
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

		Texts.on(UI.formText(comp, tk, "Laufzeit (Jahre)"))
			.required()
			.init(plant().duration)
			.integer()
			.onChanged(s -> {
				plant().duration = Num.readInt(s);
				editor.setDirty();
			});
		HelpLink.create(comp, tk, "Laufzeit (Jahre)", H.PlantDurationInfo);

		// description
		Texts.on(UI.formMultiText(comp, tk, M.Description))
			.init(plant().description)
			.onChanged(s -> {
				plant().description = s;
				editor.setDirty();
			});
		UI.filler(comp, tk);

		UI.filler(comp, tk);
		var btn = tk.createButton(comp, "Berechnen", SWT.NONE);
		btn.setImage(Icon.CALCULATE_16.img());
		Controls.onSelect(btn, _ -> {
			var res = App.exec(
				"Berechne ...",
				() -> BiogasPlantResult.calculate(plant()));
			if (res.isError()) {
				MsgBox.error("Eine Berechnung ist nicht möglich.", res.error());
				return;
			}
			BiogasPlantResultEditor.open(res.value());
		});

	}

	private void createOtherCostsSection(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk, "Sonstige Kosten");
		UI.gridLayout(comp, 3);

		t(comp, tk, "Versicherung", "%", plant().settings.insuranceCostsShare)
			.onChanged(s -> plant().settings.insuranceCostsShare = Num.read(s));

		t(comp, tk, "Sonstige Abgaben (Steuern, Pacht, usw.)", "%",
			plant().settings.otherCostsShare)
			.onChanged(s -> plant().settings.otherCostsShare = Num.read(s));
	}

	private void createPriceChangeSection(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk, "Preisänderungsfaktoren");
		UI.gridLayout(comp, 3);

		t(comp, tk, "Investitionen", "", plant().settings.investmentFactor)
			.onChanged(s -> plant().settings.investmentFactor = Num.read(s));

		t(comp, tk, "Biomasse-Brennstoff", "", plant().settings.bioFuelFactor)
			.onChanged(s -> plant().settings.bioFuelFactor = Num.read(s));

		t(comp, tk, "Fossiler Brennstoff", "", plant().settings.fossilFuelFactor)
			.onChanged(s -> plant().settings.fossilFuelFactor = Num.read(s));

		t(comp, tk, "Strom", "", plant().settings.electricityFactor)
			.onChanged(s -> plant().settings.electricityFactor = Num.read(s));

		t(comp, tk, "Lohnkosten und sonstige Kosten", "",
			plant().settings.operationFactor)
			.onChanged(s -> plant().settings.operationFactor = Num.read(s));

		t(comp, tk, "Instandhaltung", "", plant().settings.maintenanceFactor)
			.onChanged(s -> plant().settings.maintenanceFactor = Num.read(s));

		t(comp, tk, "Strommehrerlöse", "",
			plant().settings.electricityRevenuesFactor)
			.onChanged(s -> plant().settings.electricityRevenuesFactor = Num.read(s));
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
