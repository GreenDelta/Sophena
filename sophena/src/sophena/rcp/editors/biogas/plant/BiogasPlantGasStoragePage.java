package sophena.rcp.editors.biogas.plant;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.calc.biogas.BiogasPlants;
import sophena.model.biogas.BiogasPlant;
import sophena.rcp.colors.Colors;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpLink;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

/// The page for the gas storage of a biogas plant.
class BiogasPlantGasStoragePage extends FormPage {

	private final BiogasPlantEditor editor;
	private Text sizeText;
	private boolean updating;

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
		var tk = mForm.getToolkit();
		var body = UI.formBody(form, tk);

		createStorageSection(body, tk);

		editor.onResult(_ -> updateStorageSize());
		editor.calculate();
		form.reflow(true);
	}

	private void createStorageSection(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk, "Gasspeicher");
		UI.gridLayout(comp, 4);

		// gas storage size
		sizeText = UI.formText(comp, tk, "Gasspeichergröße");
		Texts.on(sizeText)
			.decimal()
			.init(BiogasPlants.gasStorageSizeOf(plant()))
			.onChanged(s -> {
				if (updating)
					return;
				var n = Num.readNumber(s);
				if (n == null) {
					plant().gasStorageSize = null;
				} else {
					double value = n.doubleValue();
					double def = BiogasPlants.defaultGasStorageSizeOf(plant());
					plant().gasStorageSize = Num.equal(value, def)
						? null
						: value;
				}
				editor.setDirty();
				editor.calculate();
			});
		UI.formLabel(comp, tk, "m³");
		UI.filler(comp, tk);

		// filling level
		var fillingText = UI.formText(comp, tk, "Gasspeicherbefüllungsanteil");
		Texts.on(fillingText)
			.decimal()
			.init(plant().gasStorageFillingLevel)
			.onChanged(s -> {
				plant().gasStorageFillingLevel = Num.read(s);
				editor.setDirty();
			});
		UI.formLabel(comp, tk, "%");
		HelpLink.create(comp, tk, "Gasspeicherbefüllungsanteil",
			H.GasStorageFillingLevelInfo);

		// temperature
		var temperatureText = UI.formText(comp, tk, "Gasspeichertemperatur");
		Texts.on(temperatureText)
			.decimal()
			.init(plant().gasStorageTemperature)
			.onChanged(s -> {
				plant().gasStorageTemperature = Num.read(s);
				editor.setDirty();
			});
		UI.formLabel(comp, tk, "°C");
		UI.filler(comp, tk);

		// overpressure
		var pressureText = UI.formText(comp, tk, "Gasspeicherüberdruck");
		Texts.on(pressureText)
			.decimal()
			.init(plant().gasStorageOverpressure)
			.onChanged(s -> {
				plant().gasStorageOverpressure = Num.read(s);
				editor.setDirty();
			});
		UI.formLabel(comp, tk, "mbar");
		UI.filler(comp, tk);

		updateStorageSize();
	}

	/// Shows the calculated default size when the user did not enter a size.
	private void updateStorageSize() {
		if (sizeText == null || sizeText.isDisposed())
			return;
		if (plant().gasStorageSize == null) {
			updating = true;
			Texts.set(sizeText, BiogasPlants.gasStorageSizeOf(plant()));
			updating = false;
		}
		sizeText.setBackground(plant().gasStorageSize == null
			? Colors.forRequiredField()
			: Colors.forModifiedDefault());
	}
}
