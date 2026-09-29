package sophena.rcp.editors.biogas.plant;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.biogas.BiogasPlant;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpLink;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class LossesSection {

	private final BiogasPlantEditor editor;

	private LossesSection(BiogasPlantEditor editor) {
		this.editor = editor;
	}

	static LossesSection of(BiogasPlantEditor editor) {
		return new LossesSection(editor);
	}

	private BiogasPlant plant() {
		return editor.plant();
	}

	void create(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk, "Verluste");
		UI.gridLayout(comp, 4);
		var settings = plant().settings;

		t(comp, tk, "Kabel- und Trafoverluste", "kW", settings.transmissionLosses)
			.onChanged(s -> settings.transmissionLosses = Num.read(s));
		HelpLink.create(comp, tk, "Kabel- und Trafoverluste",
			H.TransmissionLossesInfo);

		t(comp, tk, "Methanschlupf", "%", settings.methaneSlip)
			.onChanged(s -> settings.methaneSlip = Num.read(s));
		HelpLink.create(comp, tk, "Methanschlupf", H.MethaneSlipInfo);

		t(comp, tk, "Wärmeverluste", "kW", settings.heatLoss)
			.onChanged(s -> settings.heatLoss = Num.read(s));
		HelpLink.create(comp, tk, "Wärmeverluste", H.HeatLossInfo);
	}

	private Texts.TextBox t(
		Composite comp, FormToolkit tk, String label, String unit, double initial
	) {
		Text text = UI.formText(comp, tk, label);
		UI.formLabel(comp, tk, unit);
		return Texts.on(text)
			.decimal()
			.init(initial)
			.onChanged(_ -> editor.setDirty());
	}
}
