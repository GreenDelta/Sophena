package sophena.rcp.editors.heatnets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.Defaults;
import sophena.calc.ProjectLoad;
import sophena.model.HeatNet;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class HeatNetSection {

	private HeatNetEditor editor;

	private Composite comp;
	private FormToolkit tk;
	private Text maxSimLoadText;

	HeatNetSection(HeatNetEditor editor) {
		this.editor = editor;
	}

	private HeatNet heatNet() {
		return editor.heatNet;
	}

	void create(Composite body, FormToolkit tk) {
		this.tk = tk;
		comp = UI.formSection(body, tk, M.HeatingNetwork);
		UI.gridLayout(comp, 4);
		supplyTemperatureRow();
		returnTemperatureRow();
		maxLoadRow();
		simultaneityFactorRow();
		smoothingFactorRow();
		maxSimultaneousLoadRow();
	}

	private void supplyTemperatureRow() {
		Text t = UI.formText(comp, tk, "Vorlauftemperatur");
		Texts.on(t).init(heatNet().supplyTemperature).decimal().required()
				.onChanged(s -> {
					heatNet().supplyTemperature = Num.read(s);
					editor.setDirty();
				});
		UI.formLabel(comp, tk, "°C");
		UI.filler(comp, tk);
	}

	private void returnTemperatureRow() {
		Text t = UI.formText(comp, tk, "Rücklauftemperatur");
		Texts.on(t).init(heatNet().returnTemperature).decimal().required()
				.onChanged(s -> {
					heatNet().returnTemperature = Num.read(s);
					editor.setDirty();
				});
		UI.formLabel(comp, tk, "°C");
		UI.filler(comp, tk);
	}

	private void maxLoadRow() {
		Text t = UI.formText(comp, tk,
				"Maximal benötigte Leistung (ohne Gleichzeitigkeitsfaktor)");
		Texts.on(t).init(ProjectLoad.getMax(editor.project)).decimal().required()
				.onChanged(s -> {
					heatNet().maxLoad = Num.read(s);
					updateMaxSimLoad();
					editor.setDirty();
				});
		if (heatNet().maxLoad == null)

			UI.formLabel(comp, tk, "kW");
		tk.createButton(comp, "Standardwert", SWT.NONE)
				.setToolTipText("Auf Standardwert zurücksetzen");
	}

	private void simultaneityFactorRow() {
		Text t = UI.formText(comp, tk, "Gleichzeitigkeitsfaktor");
		Texts.set(t, heatNet().simultaneityFactor);
		Texts.on(t).decimal().required().onChanged(s -> {
			heatNet().simultaneityFactor = Texts.getDouble(t);
			updateMaxSimLoad();
			editor.setDirty();
		});
		UI.formLabel(comp, tk, "").setImage(Images.INFO_16.img());
		UI.filler(comp, tk);
	}

	private void maxSimultaneousLoadRow() {
		Text t = UI.formText(comp, tk,
				"Maximal benötigte Leistung (mit Gleichzeitigkeitsfaktor)");
		double simLoad = ProjectLoad.getSimultanousMax(editor.project);
		Texts.set(t, simLoad);
		Texts.on(t).decimal().calculated();
		UI.formLabel(comp, tk, "kW");
		maxSimLoadText = t;
		UI.filler(comp, tk);
	}

	private void smoothingFactorRow() {
		Text t = UI.formText(comp, tk, "Glättungsfaktor");
		Texts.set(t, heatNet().smoothingFactor);
		Texts.on(t).decimal().required().onChanged(s -> {
			double val = Num.read(s);
			heatNet().smoothingFactor = val;
			editor.setDirty();
			if (Num.equal(val, Defaults.SMOOTHING_FACTOR)) {
				t.setBackground(Colors.forDefaultField());
			} else {
				t.setBackground(Colors.forRequiredField());
			}
		});
		if (Num.equal(heatNet().smoothingFactor, Defaults.SMOOTHING_FACTOR))
			t.setBackground(Colors.forDefaultField());
		UI.filler(comp, tk);
		tk.createButton(comp, "Standardwert", SWT.NONE)
				.setToolTipText("Auf Standardwert zurücksetzen");
	}

	private void updateMaxSimLoad() {
		double val = ProjectLoad.getSimultanousMax(editor.project);
		Texts.set(maxSimLoadText, val);
	}

}
