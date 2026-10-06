package sophena.rcp.editors.biogas.plant;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.biogas.BiogasPlant;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpLink;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class BiogasPlantBoilerPage extends FormPage {

	private final BiogasPlantEditor editor;

	BiogasPlantBoilerPage(BiogasPlantEditor editor) {
		super(editor, "BiogasPlantBoilerPage", "BHKW");
		this.editor = editor;
	}

	private BiogasPlant plant() {
		return editor.plant();
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		var form = UI.formHeader(mForm, "BHKW - " + plant().name);
		var tk = mForm.getToolkit();
		var body = UI.formBody(form, tk);

		createGeneralInputsSection(body, tk);

		// boiler blocks
		BiogasPlantBoilerTable.of(editor).render(body, tk);

		// electricity prices
		ElectricitySection.of(editor).create(body, tk);

		// producer profile
		ProducerProfileSection.of(editor).create(body, tk);

		editor.calculate();
		form.reflow(true);
	}

	private void createGeneralInputsSection(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk, "Allgemeine Eingaben");
		UI.gridLayout(comp, 4);
		var settings = plant().settings;

		// maximum rated power
		var powerText = UI.formText(comp, tk, "Höchstbemessungsleistung");
		Texts.on(powerText)
			.decimal()
			.init(Num.str(settings.maxRatedPower))
			.onChanged(s -> {
				var n = Num.readNumber(s);
				settings.maxRatedPower = n == null ? null : n.doubleValue();
				editor.setDirty();
			});
		UI.formLabel(comp, tk, "kW");
		UI.filler(comp, tk);

		// minimum runtime of the boilers
		var runtimeText = UI.formText(comp, tk, "BHKW-Mindestlaufzeit");
		Texts.on(runtimeText)
			.integer()
			.init(plant().minimumRuntime)
			.onChanged(s -> {
				plant().minimumRuntime = Num.readInt(s);
				editor.setDirty();
				editor.calculate();
			});
		UI.formLabel(comp, tk, "h");
		HelpLink.create(comp, tk, "BHKW-Mindestlaufzeit", H.MinimumRuntimeInfo);
	}
}
