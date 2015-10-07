package sophena.rcp.editors.heatnets;

import java.util.function.DoubleConsumer;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.calc.ProjectLoad;
import sophena.model.HeatNet;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

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
		UI.gridLayout(comp, 3);

		d("Vorlauftemperatur", "°C",
				heatNet().supplyTemperature,
				(v) -> heatNet().supplyTemperature = v);

		d("Rücklauftemperatur", "°C",
				heatNet().returnTemperature,
				(v) -> heatNet().returnTemperature = v);

		d("Maximal benötigte Leistung (ohne Gleichzeitigkeitsfaktor)", "kW",
				ProjectLoad.getMax(editor.project),
				(v) -> {
					heatNet().maxLoad = v;
					updateMaxSimLoad();
				});

		createSimFactorText();
		maxSimLoadText = createMaxSimLoadText();
	}

	private Text d(String label, String unit, double init, DoubleConsumer fn) {
		Text t = UI.formText(comp, tk, label);
		Texts.on(t).init(init).decimal().required().onChanged((s) -> {
			fn.accept(Texts.getDouble(t));
			editor.setDirty();
		});
		UI.formLabel(comp, tk, unit);
		return t;
	}

	private Text createMaxSimLoadText() {
		Text t = UI.formText(comp, tk,
				"Maximal benötigte Leistung (mit Gleichzeitigkeitsfaktor)");
		double simLoad = ProjectLoad.getSimultanousMax(editor.project);
		Texts.set(t, simLoad);
		Texts.on(t).decimal().calculated();
		UI.formLabel(comp, tk, "kW");
		return t;
	}

	private void createSimFactorText() {
		Text t = UI.formText(comp, tk, "Gleichzeitigkeitsfaktor");
		Texts.set(t, heatNet().simultaneityFactor);
		Texts.on(t).decimal().required().onChanged((s) -> {
			heatNet().simultaneityFactor = Texts.getDouble(t);
			editor.setDirty();
			updateMaxSimLoad();
		});
		UI.formLabel(comp, tk, "").setImage(Images.INFO_16.img());
	}

	private void updateMaxSimLoad() {
		double val = ProjectLoad.getSimultanousMax(editor.project);
		Texts.set(maxSimLoadText, val);
	}

}
