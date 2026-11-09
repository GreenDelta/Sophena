package sophena.rcp.editors.biogas.plant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleConsumer;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import sophena.model.biogas.Fermenter;
import sophena.model.biogas.RoofType;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.rcp.wizards.SimpleWizard;
import sophena.utils.Num;

/// Wizard for creating and editing a fermenter of a biogas plant. Besides the
/// parameters of the fermenter, the successor of the fermenter can be selected
/// from the other fermenters of the plant; fermenters that would create a
/// cycle are not offered.
class FermenterWizard extends SimpleWizard {

	private static final String NO_SUCCESSOR = "-";

	private final Fermenter fermenter;
	private final List<Fermenter> successors;

	/// Opens the wizard for the given fermenter. The other fermenters of the
	/// plant are required to select the successor; the given fermenter can be
	/// part of this list.
	static int open(Fermenter fermenter, List<Fermenter> plantFermenters) {
		if (fermenter == null)
			return Window.CANCEL;
		var wizard = new FermenterWizard(
			fermenter, successorsOf(fermenter, plantFermenters));
		return wizard.open();
	}

	private FermenterWizard(Fermenter fermenter, List<Fermenter> successors) {
		super("Fermenter");
		this.fermenter = fermenter;
		this.successors = successors;
	}

	/// Returns the fermenters that can be selected as successor of the given
	/// fermenter: all other fermenters of the plant from which the substrate
	/// does not flow (directly or indirectly) into the given fermenter.
	static List<Fermenter> successorsOf(
		Fermenter fermenter, List<Fermenter> plantFermenters
	) {
		var ids = new HashMap<String, Fermenter>();
		for (var f : plantFermenters) {
			if (f != null) {
				ids.put(f.id, f);
			}
		}
		var list = new ArrayList<Fermenter>();
		for (var f : plantFermenters) {
			if (f == null || Objects.equals(f.id, fermenter.id))
				continue;
			if (!flowsInto(f, fermenter.id, ids)) {
				list.add(f);
			}
		}
		return list;
	}

	/// Returns true when the substrate flows from the given fermenter (directly
	/// or indirectly) into the fermenter with the given ID.
	private static boolean flowsInto(
		Fermenter from, String targetId, HashMap<String, Fermenter> ids
	) {
		var next = from;
		// the number of steps is limited, in case there is already a cycle
		for (int i = 0; i <= ids.size() && next != null; i++) {
			if (next.successorId == null)
				return false;
			if (Objects.equals(next.successorId, targetId))
				return true;
			next = ids.get(next.successorId);
		}
		return false;
	}

	@Override
	protected int[] minimumSize() {
		return new int[] { 700, 520 };
	}

	@Override
	protected boolean onFinish() {
		if (fermenter.roofType == null) {
			fermenter.roofType = RoofType.DOUBLE_MEMBRANE;
		}
		return true;
	}

	@Override
	protected void create(Composite comp) {
		UI.gridLayout(comp, 2).makeColumnsEqualWidth = true;
		createGeneralGroup(comp);
		createRoofGroup(comp);
		createWallGroup(comp);
		createFloorAndMixerGroup(comp);
	}

	private void createGeneralGroup(Composite parent) {
		var comp = group(parent, "Allgemein");

		var nameText = UI.formText(comp, "Name");
		Texts.on(nameText)
			.init(fermenter.name)
			.onChanged(s -> fermenter.name = s);
		UI.filler(comp);

		var combo = UI.formCombo(comp, "Nachfermenter");
		UI.filler(comp);
		initSuccessorCombo(combo);

		num(comp, "Solltemperatur", "°C",
			fermenter.targetTemperature, v -> fermenter.targetTemperature = v);
	}

	private void initSuccessorCombo(Combo combo) {
		var items = new String[successors.size() + 1];
		items[0] = NO_SUCCESSOR;
		int selected = 0;
		for (int i = 0; i < successors.size(); i++) {
			var s = successors.get(i);
			items[i + 1] = labelOf(s);
			if (Objects.equals(s.id, fermenter.successorId)) {
				selected = i + 1;
			}
		}
		combo.setItems(items);
		combo.select(selected);
		// a successor that cannot be selected (anymore) is removed
		if (selected == 0) {
			fermenter.successorId = null;
		}
		Controls.onSelect(combo, _ -> {
			int i = combo.getSelectionIndex();
			fermenter.successorId = i > 0
				? successors.get(i - 1).id
				: null;
		});
	}

	static String labelOf(Fermenter f) {
		if (f == null)
			return "";
		return f.name != null && !f.name.isBlank()
			? f.name
			: "Fermenter ohne Namen";
	}

	private void createWallGroup(Composite parent) {
		var comp = group(parent, "Zylinderwand");
		num(comp, "Außenradius (inkl. Dämmung)", "m",
			fermenter.wallOuterRadius, v -> fermenter.wallOuterRadius = v);
		num(comp, "Dicke der tragenden Wand", "m",
			fermenter.wallStructuralThickness,
			v -> fermenter.wallStructuralThickness = v);
		num(comp, "Dämmstärke", "m",
			fermenter.wallInsulationThickness,
			v -> fermenter.wallInsulationThickness = v);
		num(comp, "Wandhöhe gesamt", "m",
			fermenter.wallTotalHeight, v -> fermenter.wallTotalHeight = v);
		num(comp, "Erdberührter Wandanteil (0..1)", "-",
			fermenter.wallBuriedFraction, v -> fermenter.wallBuriedFraction = v);
		num(comp, "Verschattungsfaktor (0..1)", "-",
			fermenter.wallShadingFraction, v -> fermenter.wallShadingFraction = v);
	}

	private void createRoofGroup(Composite parent) {
		var comp = group(parent, "Fermenterdach");

		UI.formLabel(comp, "Dachtyp");
		var radioComp = new Composite(comp, SWT.NONE);
		UI.innerGrid(radioComp, 2);
		UI.gridData(radioComp, true, false).horizontalSpan = 2;
		var membraneRadio = new Button(
			radioComp, SWT.RADIO);
		membraneRadio.setText("Doppelmembrandach");
		var fixedRadio = new Button(radioComp, SWT.RADIO);
		fixedRadio.setText("Festes Dach");
		fixedRadio.setSelection(fermenter.roofType == RoofType.FIXED);
		membraneRadio.setSelection(fermenter.roofType != RoofType.FIXED);

		var membraneHeight = num(comp, "Membranhöhe", "m",
			fermenter.roofMembraneHeight, v -> fermenter.roofMembraneHeight = v);
		var fixedThickness = num(comp, "Dachschichtdicke", "m",
			fermenter.roofFixedLayerThickness,
			v -> fermenter.roofFixedLayerThickness = v);
		var fixedInsulation = num(comp, "Dämmstärke", "m",
			fermenter.roofInsulationThickness,
			v -> fermenter.roofInsulationThickness = v);
		num(comp, "Verschattungsfaktor (0..1)", "-",
			fermenter.roofShadingFraction, v -> fermenter.roofShadingFraction = v);

		Runnable update = () -> {
			if (fixedRadio.getSelection()) {
				fermenter.roofType = RoofType.FIXED;
				fixedThickness.enable();
				fixedInsulation.enable();
				membraneHeight.disable();
			} else {
				fermenter.roofType = RoofType.DOUBLE_MEMBRANE;
				fixedThickness.disable();
				fixedInsulation.disable();
				membraneHeight.enable();
			}
		};
		Controls.onSelect(fixedRadio, _ -> update.run());
		Controls.onSelect(membraneRadio, _ -> update.run());
		update.run();
	}

	private void createFloorAndMixerGroup(Composite parent) {
		var floor = group(parent, "Bodenplatte und Rührwerk");
		num(floor, "Dicke der Bodenplatte", "m",
			fermenter.floorSlabThickness, v -> fermenter.floorSlabThickness = v);
		num(floor, "Dämmstärke Bodenplatte", "m",
			fermenter.floorInsulationThickness,
			v -> fermenter.floorInsulationThickness = v);
		num(floor, "Leistungsdichte Rührwerk", "W/m³",
			fermenter.mixerPowerDensity, v -> fermenter.mixerPowerDensity = v);
		num(floor, "Laufzeit Rührwerk", "min/h",
			fermenter.mixerRuntime, v -> fermenter.mixerRuntime = v);
		num(floor, "Wärmeeintragsfaktor (0..1)", "-",
			fermenter.mixerHeatFraction, v -> fermenter.mixerHeatFraction = v);
	}

	private Composite group(Composite parent, String title) {
		var group = new Group(parent, SWT.NONE);
		group.setText(title);
		UI.gridData(group, true, true);
		UI.gridLayout(group, 3);
		return group;
	}

	private Texts.TextBox num(
		Composite comp, String label, String unit, double initial,
		DoubleConsumer setter
	) {
		var text = UI.formText(comp, label);
		UI.formLabel(comp, unit);
		return Texts.on(text)
			.decimal()
			.init(initial)
			.onChanged(s -> setter.accept(Num.read(s)));
	}
}
