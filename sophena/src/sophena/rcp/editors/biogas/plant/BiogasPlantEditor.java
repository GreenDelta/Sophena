package sophena.rcp.editors.biogas.plant;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;

import sophena.model.biogas.BiogasPlant;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.editors.Editor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

public class BiogasPlantEditor extends Editor {

	private BiogasPlant plant;

	public static void createNew() {
		BiogasPlantWizard.open().ifPresent(BiogasPlantEditor::open);
	}

	public static void open(BiogasPlant plant) {
		if (plant == null)
			return;
		var input = new KeyEditorInput(plant.id, plant.name);
		Editors.open(input, "sophena.BiogasPlantEditor");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		var keyInp = (KeyEditorInput) input;
		plant = App.getDb().get(BiogasPlant.class, keyInp.getKey());
		if (plant == null)
			throw new PartInitException("biogas plant does not exists: " + keyInp.getKey());
		setPartName(plant.name);
	}

	@Override
	protected void addPages() {
		try {
			addPage(new Page(this));
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			plant = App.getDb().update(plant);
			setPartName(plant.name);
			Navigator.refresh();
			setSaved();
		} catch (Exception e) {
			log.error("failed to save biogas plant", e);
		}
	}

	private static class Page extends FormPage {

		private final BiogasPlantEditor editor;

		Page(BiogasPlantEditor editor) {
			super(editor, "BiogasPlantPage", "Biogasanlage");
			this.editor = editor;
		}

		private BiogasPlant plant() {
			return editor.plant;
		}

		@Override
		protected void createFormContent(IManagedForm mForm) {
			var form = UI.formHeader(mForm, plant().name);
			var tk = mForm.getToolkit();
			var body = UI.formBody(form, tk);

			var comp = UI.formSection(body, tk, "BHKW");

			// name
			Texts.on(UI.formText(comp, tk, M.Name))
					.required()
					.init(plant().name)
					.onChanged(s -> {
						plant().name = s;
						editor.setDirty();
					});

			// product group
			Controls.renderGroupLink(plant().productGroup, tk, comp);

			// description
			Texts.on(UI.formMultiText(comp, tk, M.Description))
					.init(plant().description)
					.onChanged(s -> {
						plant().description = s;
						editor.setDirty();
					});
		}
	}

}
