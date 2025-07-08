package sophena.rcp.editors.biogas.plant;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import sophena.model.biogas.BiogasPlant;
import sophena.rcp.App;
import sophena.rcp.editors.Editor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

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

	BiogasPlant plant() {
		return plant;
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
			addPage(new BiogasPlantInfoPage(this));
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

}
