package sophena.rcp.editors.biogas.plant;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.openlca.commons.Res;

import sophena.calc.biogas.BiogasPlantResult;
import sophena.model.biogas.BiogasPlant;
import sophena.rcp.app.App;
import sophena.rcp.editors.Editor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class BiogasPlantEditor extends Editor {

	private BiogasPlant plant;
	private final List<Consumer<BiogasPlantResult>> resultFns = new ArrayList<>();

	/// The result of the last calculation, `null` before the first calculation.
	/// When the plant cannot be calculated, it contains the error and the
	/// reason why, see `BiogasPlantInfoPage`.
	private Res<BiogasPlantResult> calculation;

	public static void open(BiogasPlant plant) {
		if (plant == null)
			return;
		var input = new KeyEditorInput(plant.id, plant.name);
		Editors.open(input, "sophena.BiogasPlantEditor");
	}

	BiogasPlant plant() {
		return plant;
	}

	void calculate() {
		calculation = BiogasPlantResult.calculate(plant);
		// the pages show empty charts when the plant cannot be calculated
		var result = calculation.isError()
			? BiogasPlantResult.emptyOf(plant)
			: calculation.value();
		for (var fn : resultFns) {
			fn.accept(result);
		}
	}

	/// The result of the last calculation with the reason why when the plant
	/// cannot be calculated.
	Res<BiogasPlantResult> calculation() {
		if (calculation == null) {
			calculation = BiogasPlantResult.calculate(plant);
		}
		return calculation;
	}

	void onResult(Consumer<BiogasPlantResult> fn) {
		if (fn != null) {
			resultFns.add(fn);
		}
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
			addPage(new BiogasPlantCostSettingsPage(this));
			addPage(new FermenterPage(this));
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
			App.events().emit(plant);
		} catch (Exception e) {
			log.error("failed to save biogas plant", e);
		}
	}

}
