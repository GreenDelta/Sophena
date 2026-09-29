package sophena.rcp.editors.biogas.plant;

import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.db.daos.FuelDao;
import sophena.model.Fuel;
import sophena.model.FuelGroup;
import sophena.model.biogas.BiogasPlant;
import sophena.rcp.M;
import sophena.rcp.app.App;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpLink;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
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

		createGeneralSection(body, tk);

		createSettingsSection(body, tk);

		// biogas boilers
		BiogasPlantBoilerTable.of(editor).render(body, tk);

		// substrate section
		var substrateSection = SubstrateSection.of(editor);
		substrateSection.create(body, tk);

		// electricity section
		ElectricitySection.of(editor).create(body, tk);

		// producer profile section
		ProducerProfileSection.of(editor).create(body, tk);
		editor.calculate();
	}

	private void createSettingsSection(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk, "Allgemeine Einstellungen");
		UI.gridLayout(comp, 3);

		var storageText = UI.formText(comp, tk, "Gasspeichergröße");
		Texts.on(storageText)
			.decimal()
			.init(plant().gasStorageSize)
			.onChanged(s -> {
				plant().gasStorageSize = Num.read(s);
				editor.setDirty();
				editor.calculate();
			});
		UI.formLabel(comp, tk, "m3");

		var runtimeText = UI.formText(comp, tk, "Mindestlaufzeit");
		Texts.on(runtimeText)
			.integer()
			.init(plant().minimumRuntime)
			.onChanged(s -> {
				plant().minimumRuntime = Num.readInt(s);
				editor.setDirty();
				editor.calculate();
			});
		UI.formLabel(comp, tk, "h");
	}

	private void createGeneralSection(Composite body, FormToolkit tk) {
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
		UI.gridData(combo.getControl(), false, false).widthHint = 235;
		var fuels = new FuelDao(App.getDb())
			.getAll()
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

		Button fullFeedIn = tk.createButton(
				radioComp, "Volleinspeisung", SWT.RADIO);
		Button surplusFeedIn = tk.createButton(
				radioComp, "Überschusseinspeisung", SWT.RADIO);

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
			Composite comp, FormToolkit tk, String label, String unit, double initial) {
		Text text = UI.formText(comp, tk, label);
		UI.formLabel(comp, tk, unit);
		return Texts.on(text)
				.decimal()
				.init(initial)
				.onChanged(_ -> editor.setDirty());
	}
}
