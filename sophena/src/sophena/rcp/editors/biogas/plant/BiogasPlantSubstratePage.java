package sophena.rcp.editors.biogas.plant;

import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;

import sophena.model.biogas.BiogasPlant;
import sophena.rcp.utils.UI;

class BiogasPlantSubstratePage extends FormPage {

	private final BiogasPlantEditor editor;

	BiogasPlantSubstratePage(BiogasPlantEditor editor) {
		super(editor, "BiogasPlantSubstratePage", "Substrate");
		this.editor = editor;
	}

	private BiogasPlant plant() {
		return editor.plant();
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		var form = UI.formHeader(mForm, "Substrate - " + plant().name);
		var tk = mForm.getToolkit();
		var body = UI.formBody(form, tk);

		SubstrateSection.of(editor).create(body, tk);

		editor.calculate();
		form.reflow(true);
	}
}
