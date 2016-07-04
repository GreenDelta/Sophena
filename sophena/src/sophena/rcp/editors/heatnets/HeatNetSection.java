package sophena.rcp.editors.heatnets;

import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.Defaults;
import sophena.calc.ProjectLoad;
import sophena.db.daos.ProjectDao;
import sophena.model.Consumer;
import sophena.model.HeatNet;
import sophena.model.Project;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.help.HelpLink;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.Controls;
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
		Function<Double, Boolean> isDefault = val -> {
			if (val == null)
				return true;
			double max = calculateMaxLoad();
			return Num.equal(max, val);
		};
		Texts.on(t).init(ProjectLoad.getMax(editor.project)).decimal().required()
				.onChanged(s -> {
					double val = Num.read(s);
					if (isDefault.apply(val)) {
						heatNet().maxLoad = null;
						t.setBackground(Colors.forRequiredField());
					} else {
						heatNet().maxLoad = val;
						t.setBackground(Colors.forModifiedDefault());
					}
					Texts.set(maxSimLoadText, calculateMaxSimLoad());
					editor.setDirty();
				});
		if (!isDefault.apply(heatNet().maxLoad))
			t.setBackground(Colors.forModifiedDefault());
		UI.formLabel(comp, tk, "kW");
		Button reset = tk.createButton(comp, "Standardwert", SWT.NONE);
		reset.setToolTipText("Auf Standardwert zurücksetzen");
		Controls.onSelect(reset, e -> Texts.set(t, calculateMaxLoad()));
	}

	private void simultaneityFactorRow() {
		Text t = UI.formText(comp, tk, "Gleichzeitigkeitsfaktor");
		Texts.set(t, heatNet().simultaneityFactor);
		Texts.on(t).decimal().required().onChanged(s -> {
			heatNet().simultaneityFactor = Texts.getDouble(t);
			Texts.set(maxSimLoadText, calculateMaxSimLoad());
			editor.setDirty();
		});
		HelpLink.create(comp, tk, "Gleichzeitigkeitsfaktor", "noch nicht verfügbar");
		UI.filler(comp, tk);
	}

	private void maxSimultaneousLoadRow() {
		Text t = UI.formText(comp, tk,
				"Maximal benötigte Leistung (mit Gleichzeitigkeitsfaktor)");
		Texts.set(t, calculateMaxSimLoad());
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
				t.setBackground(Colors.forRequiredField());
			} else {
				t.setBackground(Colors.forModifiedDefault());
			}
		});
		if (!Num.equal(heatNet().smoothingFactor, Defaults.SMOOTHING_FACTOR))
			t.setBackground(Colors.forModifiedDefault());
		UI.filler(comp, tk);
		Button reset = tk.createButton(comp, "Standardwert", SWT.NONE);
		reset.setToolTipText("Auf Standardwert zurücksetzen");
		Controls.onSelect(reset, e -> Texts.set(t, Defaults.SMOOTHING_FACTOR));
	}

	private double calculateMaxSimLoad() {
		HeatNet net = heatNet();
		double max = net.maxLoad == null ? calculateMaxLoad() : net.maxLoad;
		double sim = net.simultaneityFactor * max;
		return Math.ceil(sim);
	}

	private double calculateMaxLoad() {
		try {
			// net load from the net-specification on this page
			double load = ProjectLoad.getNetLoad(heatNet());
			// add consumer loads from the consumers in the database
			// we load them freshly from the database because the may changed
			// in other editors
			ProjectDao dao = new ProjectDao(App.getDb());
			Project p = dao.get(editor.project.id);
			for (Consumer c : p.consumers) {
				if (c.disabled)
					continue;
				load += c.heatingLoad;
			}
			return Math.ceil(load);
		} catch (Exception e) {
			return 0;
		}
	}

}
