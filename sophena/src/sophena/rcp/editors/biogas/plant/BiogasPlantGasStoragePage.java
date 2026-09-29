package sophena.rcp.editors.biogas.plant;

import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;

import sophena.model.biogas.BiogasPlant;
import sophena.rcp.utils.UI;

/// The page for the gas storage of a biogas plant. It is empty for now; the
/// settings of the gas storage are still edited in the information page.
class BiogasPlantGasStoragePage extends FormPage {

	private final BiogasPlantEditor editor;

	BiogasPlantGasStoragePage(BiogasPlantEditor editor) {
		super(editor, "BiogasPlantGasStoragePage", "Gasspeicher");
		this.editor = editor;
	}

	private BiogasPlant plant() {
		return editor.plant();
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		var form = UI.formHeader(mForm, "Gasspeicher - " + plant().name);
		UI.formBody(form, mForm.getToolkit());
		form.reflow(true);
	}
}
