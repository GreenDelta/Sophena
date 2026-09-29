package sophena.rcp.editors.biogas.plant;

import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;

import sophena.model.biogas.BiogasPlant;
import sophena.rcp.utils.UI;

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

		// boiler blocks
		BiogasPlantBoilerTable.of(editor).render(body, tk);

		// electricity prices
		ElectricitySection.of(editor).create(body, tk);

		// producer profile
		ProducerProfileSection.of(editor).create(body, tk);

		editor.calculate();
		form.reflow(true);
	}
}
