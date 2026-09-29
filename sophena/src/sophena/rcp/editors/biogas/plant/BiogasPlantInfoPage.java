package sophena.rcp.editors.biogas.plant;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.biogas.BiogasPlant;
import sophena.rcp.M;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpLink;
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

		GeneralSection.of(editor).create(body, tk);

		LossesSection.of(editor).create(body, tk);

		FinancingSection.of(editor).create(body, tk);

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
}
