package sophena.rcp.editors.biogas.plant;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.biogas.Fermenter;
import sophena.model.biogas.RoofType;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class FermenterPage extends FormPage {

	private final BiogasPlantEditor editor;
	private FormToolkit tk;

	FermenterPage(BiogasPlantEditor editor) {
		super(editor, "FermenterPage", "Fermenter");
		this.editor = editor;
	}

	private Fermenter fermenter() {
		return editor.plant().fermenter;
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		var form = UI.formHeader(mForm, "Fermenter - " + editor.plant().name);
		tk = mForm.getToolkit();
		var body = UI.formBody(form, tk);
		createGeneralSection(body);
		createWallSection(body);
		createRoofSection(body);
		createFloorSection(body);
		createMixerSection(body);
		form.reflow(true);
	}

	private void createGeneralSection(Composite body) {
		var comp = UI.formSection(body, tk, "Allgemein");
		UI.gridLayout(comp, 3);
		t(comp, "Solltemperatur Substrat/Fermenter", "°C", fermenter().targetTemperature)
			.onChanged(s -> fermenter().targetTemperature = Num.read(s));
	}

	private void createWallSection(Composite body) {
		var comp = UI.formSection(body, tk, "Zylinderwand");
		UI.gridLayout(comp, 3);

		t(comp, "Außenradius (inkl. Dämmung)", "m", fermenter().wallOuterRadius)
			.onChanged(s -> fermenter().wallOuterRadius = Num.read(s));

		t(comp, "Dicke tragende Wand", "m", fermenter().wallStructuralThickness)
			.onChanged(s -> fermenter().wallStructuralThickness = Num.read(s));

		t(comp, "Dämmstärke Wand", "m", fermenter().wallInsulationThickness)
			.onChanged(s -> fermenter().wallInsulationThickness = Num.read(s));

		t(comp, "Wandhöhe gesamt", "m", fermenter().wallTotalHeight)
			.onChanged(s -> fermenter().wallTotalHeight = Num.read(s));

		t(comp, "Erdberührter Wandanteil (0..1)", "-", fermenter().wallBuriedFraction)
			.onChanged(s -> fermenter().wallBuriedFraction = Num.read(s));

		t(comp, "Verschattung Wand (0..1)", "-", fermenter().wallShadingFraction)
			.onChanged(s -> fermenter().wallShadingFraction = Num.read(s));
	}

	private void createRoofSection(Composite body) {
		var comp = UI.formSection(body, tk, "Dach");
		UI.gridLayout(comp, 3);

		// Roof type radio buttons
		UI.formLabel(comp, tk, "Dachtyp");
		var radioComp = tk.createComposite(comp);
		UI.gridLayout(radioComp, 2).marginHeight = 0;
		UI.gridData(radioComp, true, false).horizontalSpan = 2;

		var fixedRadio = tk.createButton(radioComp, "Festes Dach", SWT.RADIO);
		var membraneRadio = tk.createButton(radioComp, "Doppelmembrandach", SWT.RADIO);
		fixedRadio.setSelection(fermenter().roofType == RoofType.FIXED);
		membraneRadio.setSelection(fermenter().roofType == RoofType.DOUBLE_MEMBRANE);

		// Fixed-roof specific container
		var fixedComp = tk.createComposite(comp);
		UI.innerGrid(fixedComp, 3);
		var fixedData = UI.gridData(fixedComp, true, false);
		fixedData.horizontalSpan = 3;

		t(fixedComp, "Dachschichtdicke", "m", fermenter().roofFixedLayerThickness)
			.onChanged(s -> fermenter().roofFixedLayerThickness = Num.read(s));

		t(fixedComp, "Dämmstärke Dach", "m", fermenter().roofInsulationThickness)
			.onChanged(s -> fermenter().roofInsulationThickness = Num.read(s));

		// Double-membrane specific container
		var membraneComp = tk.createComposite(comp);
		UI.innerGrid(membraneComp, 3);
		var membraneData = UI.gridData(membraneComp, true, false);
		membraneData.horizontalSpan = 3;

		t(membraneComp, "Membranhöhe", "m", fermenter().roofMembraneHeight)
			.onChanged(s -> fermenter().roofMembraneHeight = Num.read(s));

		// Shading (applicable to both roof types)
		t(comp, "Verschattung Dach (0..1)", "-", fermenter().roofShadingFraction)
			.onChanged(s -> fermenter().roofShadingFraction = Num.read(s));

		// Setup event listeners for the radios
		Controls.onSelect(fixedRadio, e -> {
			if (fixedRadio.getSelection()) {
				fermenter().roofType = RoofType.FIXED;
				editor.setDirty();
				updateRoofControls(fixedComp, fixedData, membraneComp, membraneData);
			}
		});
		Controls.onSelect(membraneRadio, e -> {
			if (membraneRadio.getSelection()) {
				fermenter().roofType = RoofType.DOUBLE_MEMBRANE;
				editor.setDirty();
				updateRoofControls(fixedComp, fixedData, membraneComp, membraneData);
			}
		});

		// Initial state
		updateRoofControls(fixedComp, fixedData, membraneComp, membraneData);
	}

	private void updateRoofControls(
		Composite fixedComp, GridData fixedData,
		Composite membraneComp, GridData membraneData
	) {
		boolean isFixed = fermenter().roofType == RoofType.FIXED;
		fixedComp.setVisible(isFixed);
		fixedData.exclude = !isFixed;
		fixedComp.requestLayout();

		boolean isMembrane = fermenter().roofType == RoofType.DOUBLE_MEMBRANE;
		membraneComp.setVisible(isMembrane);
		membraneData.exclude = !isMembrane;
		membraneComp.requestLayout();

		if (getManagedForm() != null && getManagedForm().getForm() != null) {
			getManagedForm().getForm().reflow(true);
		}
	}

	private void createFloorSection(Composite body) {
		var comp = UI.formSection(body, tk, "Bodenplatte");
		UI.gridLayout(comp, 3);

		t(comp, "Dicke Bodenplatte", "m", fermenter().floorSlabThickness)
			.onChanged(s -> fermenter().floorSlabThickness = Num.read(s));

		t(comp, "Dämmstärke Boden", "m", fermenter().floorInsulationThickness)
			.onChanged(s -> fermenter().floorInsulationThickness = Num.read(s));
	}

	private void createMixerSection(Composite body) {
		var comp = UI.formSection(body, tk, "Rührwerk");
		UI.gridLayout(comp, 3);

		t(comp, "Leistungsdichte", "W/m³", fermenter().mixerPowerDensity)
			.onChanged(s -> fermenter().mixerPowerDensity = Num.read(s));

		t(comp, "Laufzeit", "min/h", fermenter().mixerRuntime)
			.onChanged(s -> fermenter().mixerRuntime = Num.read(s));

		t(comp, "Wärmeeintrag-Anteil (0..1)", "-", fermenter().mixerHeatFraction)
			.onChanged(s -> fermenter().mixerHeatFraction = Num.read(s));
	}

	private Texts.TextBox t(Composite comp, String label, String unit, double initial) {
		Text text = UI.formText(comp, tk, label);
		UI.formLabel(comp, tk, unit);
		return Texts.on(text)
			.decimal()
			.init(initial)
			.onChanged(s -> editor.setDirty());
	}
}
