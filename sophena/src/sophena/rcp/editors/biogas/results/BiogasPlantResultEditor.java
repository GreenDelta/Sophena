package sophena.rcp.editors.biogas.results;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.openlca.commons.Strings;

import sophena.calc.biogas.BiogasRuntimeResult;
import sophena.rcp.app.App;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

/// Shows the results of a biogas plant calculation. The result is passed to the
/// editor through a key in the application cache, see `App#stash(Object)`.
public class BiogasPlantResultEditor extends Editor {

	private BiogasRuntimeResult result;

	/// Opens the editor for the given calculation result. An already open
	/// result editor for the same plant is closed first.
	public static void open(BiogasRuntimeResult result) {
		if (result == null || result.plant() == null)
			return;
		var plant = result.plant();
		Editors.closeIf(e -> e instanceof BiogasPlantResultEditor editor
			&& editor.result != null
			&& editor.result.plant() != null
			&& Strings.equalsIgnoreCase(
				plant.id, editor.result.plant().id));

		var name = Strings.isBlank(plant.name)
			? "Biogasanlage"
			: plant.name;
		var key = App.stash(result);
		var input = new KeyEditorInput(key, name + " - Ergebnisse");
		Editors.open(input, "sophena.BiogasPlantResultEditor");
	}

	BiogasRuntimeResult result() {
		return result;
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
		throws PartInitException {
		super.init(site, input);
		if (!(input instanceof KeyEditorInput keyInput))
			throw new PartInitException("invalid editor input");
		result = App.pop(keyInput.getKey());
		if (result == null)
			throw new PartInitException("no result given for the editor");
		setPartName(keyInput.getName());
	}

	@Override
	protected void addPages() {
		try {
			addPage(new BiogasPlantResultPage(this));
		} catch (Exception e) {
			log.error("failed to add the result page", e);
		}
	}
}
