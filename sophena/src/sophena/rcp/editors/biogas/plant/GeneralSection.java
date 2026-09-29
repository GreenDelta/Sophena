package sophena.rcp.editors.biogas.plant;

import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.Fuel;
import sophena.model.FuelGroup;
import sophena.model.biogas.BiogasPlant;
import sophena.rcp.app.App;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpLink;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class GeneralSection {

	private final BiogasPlantEditor editor;

	private GeneralSection(BiogasPlantEditor editor) {
		this.editor = editor;
	}

	static GeneralSection of(BiogasPlantEditor editor) {
		return new GeneralSection(editor);
	}

	private BiogasPlant plant() {
		return editor.plant();
	}

	void create(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk, "Allgemein");
		UI.gridLayout(comp, 4);
		var settings = plant().settings;

		t(comp, tk, "Mittlerer Stundenlohn", "EUR", settings.hourlyWage)
			.onChanged(s -> settings.hourlyWage = Num.read(s));
		UI.filler(comp, tk);

		t(comp, tk, "Strompreis", "EUR/kWh", settings.electricityPrice)
			.onChanged(s -> settings.electricityPrice = Num.read(s));
		HelpLink.create(comp, tk, "Strompreis", H.ElectricityPriceInfo);

		t(comp, tk, "Verkaufspreis Biomethan aus Wirtschaftsdünger",
			"EUR/kWh", settings.manureBiomethanePrice)
			.onChanged(s -> settings.manureBiomethanePrice = Num.read(s));
		HelpLink.create(comp, tk, "Verkaufspreis Biomethan aus Wirtschaftsdünger",
			H.ManureBiomethanePriceInfo);

		t(comp, tk, "Verkaufspreis Biomethan aus Nicht-Wirtschaftsdünger",
			"EUR/kWh", settings.nonManureBiomethanePrice)
			.onChanged(s -> settings.nonManureBiomethanePrice = Num.read(s));
		HelpLink.create(comp, tk,
			"Verkaufspreis Biomethan aus Nicht-Wirtschaftsdünger",
			H.NonManureBiomethanePriceInfo);

		t(comp, tk, "Durchschnittlicher Eigenstrombedarf", "kW",
			settings.avgPowerDemand)
			.onChanged(s -> settings.avgPowerDemand = Num.read(s));
		HelpLink.create(comp, tk, "Durchschnittlicher Eigenstrombedarf",
			H.AvgPowerDemandInfo);

		createElectricityMixRow(comp, tk);
		createFeedInModeRow(comp, tk);
	}

	private void createElectricityMixRow(Composite comp, FormToolkit tk) {
		var combo = new EntityCombo<Fuel>()
			.create("Verbrauchter Strom", comp, tk);
		UI.gridData(combo.getControl(), true, false);
		var fuels = App.getDb()
			.getAll(Fuel.class)
			.stream()
			.filter(f -> f.group == FuelGroup.ELECTRICITY)
			.sorted(Sorters.byName())
			.collect(Collectors.toList());
		combo.setInput(fuels);
		combo.select(plant().settings.demandElectricityMix);
		combo.onSelect(f -> {
			plant().settings.demandElectricityMix = f;
			editor.setDirty();
		});
		UI.filler(comp, tk);
		HelpLink.create(comp, tk, "Verbrauchter Strom", H.DemandElectricityMixInfo);
	}

	private void createFeedInModeRow(Composite comp, FormToolkit tk) {
		UI.formLabel(comp, tk, "Einspeisemodus");
		var radioComp = tk.createComposite(comp);
		UI.innerGrid(radioComp, 2);
		UI.gridData(radioComp, true, false).horizontalSpan = 2;

		var surplusFeedIn = tk.createButton(
			radioComp, "Überschusseinspeisung", SWT.RADIO);
		var fullFeedIn = tk.createButton(
			radioComp, "Volleinspeisung", SWT.RADIO);

		fullFeedIn.setSelection(plant().settings.isFullFeedIn);
		surplusFeedIn.setSelection(!plant().settings.isFullFeedIn);

		Controls.onSelect(fullFeedIn, _ -> {
			plant().settings.isFullFeedIn = fullFeedIn.getSelection();
			editor.setDirty();
		});
		Controls.onSelect(surplusFeedIn, _ -> {
			plant().settings.isFullFeedIn = !surplusFeedIn.getSelection();
			editor.setDirty();
		});

		HelpLink.create(comp, tk, "Einspeisemodus", H.FeedInModeInfo);
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
