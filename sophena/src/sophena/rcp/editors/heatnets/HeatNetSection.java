package sophena.rcp.editors.heatnets;

import java.util.Arrays;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.Defaults;
import sophena.calc.ProjectLoad;
import sophena.db.daos.ProjectDao;
import sophena.math.energetic.HeatNets;
import sophena.model.Consumer;
import sophena.model.HeatNet;
import sophena.model.Project;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.editors.LoadCurveSection;
import sophena.rcp.help.H;
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
	private Text lengthText;
	private Text powerLossText;
	private Text maxSimLoadText;

	LoadCurveSection loadCurve;

	HeatNetSection(HeatNetEditor editor) {
		this.editor = editor;
	}

	private HeatNet net() {
		return editor.heatNet;
	}

	void create(Composite body, FormToolkit tk) {
		this.tk = tk;
		comp = UI.formSection(body, tk, M.HeatingNetwork);
		UI.gridLayout(comp, 4);
		supplyTemperatureRow();
		returnTemperatureRow();
		lengthAndPowerLossRow();
		maxLoadRow();
		simultaneityFactorRow();
		smoothingFactorRow();
		maxSimultaneousLoadRow();
		editor.bus.on(Arrays.asList(
				"pipes", "supplyTemperature", "returnTemperature"),
				this::colorTexts);
		colorTexts();
	}

	private void supplyTemperatureRow() {
		Text t = UI.formText(comp, tk, "Vorlauftemperatur");
		Texts.on(t).init(net().supplyTemperature).decimal().required()
				.onChanged(s -> {
					net().supplyTemperature = Num.read(s);
					editor.bus.notify("supplyTemperature");
					editor.setDirty();
				});
		UI.formLabel(comp, tk, "°C");
		UI.filler(comp, tk);
	}

	private void returnTemperatureRow() {
		Text t = UI.formText(comp, tk, "Rücklauftemperatur");
		Texts.on(t).init(net().returnTemperature).decimal().required()
				.onChanged(s -> {
					net().returnTemperature = Num.read(s);
					editor.bus.notify("returnTemperature");
					editor.setDirty();
				});
		UI.formLabel(comp, tk, "°C");
		UI.filler(comp, tk);
	}

	private void lengthAndPowerLossRow() {
		lengthText = UI.formText(comp, tk, "Trassenlänge");
		Texts.on(lengthText).init(net().length).decimal().required()
				.onChanged(s -> {
					net().length = Texts.getDouble(lengthText);
					textsUpdated();
				});
		UI.formLabel(comp, tk, "m");

		Button button = tk.createButton(comp, "Berechnen", SWT.NONE);
		button.setImage(Icon.CALCULATE_16.img());
		powerLossText = UI.formText(comp, tk, "Verlustleistung");
		Texts.on(powerLossText).init(net().powerLoss).decimal().required()
				.onChanged(s -> {
					net().powerLoss = Texts.getDouble(powerLossText);
					textsUpdated();
				});
		UI.formLabel(comp, tk, "W/m");
		UI.formLabel(comp, "");

		Controls.onSelect(button, e -> {
			HeatNet net = net();
			net.length = HeatNets.getTotalSupplyLength(net);
			net.powerLoss = HeatNets.calculatePowerLoss(net);
			Texts.set(lengthText, net.length);
			Texts.set(powerLossText, net.powerLoss);
			textsUpdated();
		});
	}

	private void textsUpdated() {
		editor.setDirty();
		if (loadCurve != null) {
			loadCurve.setData(NetLoadProfile.get(net()));
		}
		colorTexts();
	}

	private void colorTexts() {
		HeatNet net = net();
		if (net.pipes.isEmpty()) {
			lengthText.setBackground(Colors.forRequiredField());
			powerLossText.setBackground(Colors.forRequiredField());
			return;
		}
		if (Num.equal(net.powerLoss, HeatNets.calculatePowerLoss(net))) {
			powerLossText.setBackground(Colors.forRequiredField());
		} else {
			powerLossText.setBackground(Colors.forModifiedDefault());
		}
		if (Num.equal(net.length, HeatNets.getTotalSupplyLength(net))) {
			lengthText.setBackground(Colors.forRequiredField());
		} else {
			lengthText.setBackground(Colors.forModifiedDefault());
		}
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
		Texts.on(t).init(ProjectLoad.getMax(editor.project)).decimal()
				.required()
				.onChanged(s -> {
					double val = Num.read(s);
					if (isDefault.apply(val)) {
						net().maxLoad = null;
						t.setBackground(Colors.forRequiredField());
					} else {
						net().maxLoad = val;
						t.setBackground(Colors.forModifiedDefault());
					}
					Texts.set(maxSimLoadText, calculateMaxSimLoad());
					editor.setDirty();
				});
		if (!isDefault.apply(net().maxLoad))
			t.setBackground(Colors.forModifiedDefault());
		UI.formLabel(comp, tk, "kW");
		Button reset = tk.createButton(comp, "Standardwert", SWT.NONE);
		reset.setToolTipText("Auf Standardwert zurücksetzen");
		Controls.onSelect(reset, e -> Texts.set(t, calculateMaxLoad()));
	}

	private void simultaneityFactorRow() {
		Text t = UI.formText(comp, tk, "Gleichzeitigkeitsfaktor");
		Texts.set(t, net().simultaneityFactor);
		Texts.on(t).decimal().required().onChanged(s -> {
			net().simultaneityFactor = Texts.getDouble(t);
			Texts.set(maxSimLoadText, calculateMaxSimLoad());
			editor.setDirty();
		});
		HelpLink.create(comp, tk, "Gleichzeitigkeitsfaktor",
				H.SimultaneityFactor);
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
		Texts.set(t, net().smoothingFactor);
		Texts.on(t).decimal().required().onChanged(s -> {
			double val = Num.read(s);
			net().smoothingFactor = val;
			editor.setDirty();
			if (Num.equal(val, Defaults.SMOOTHING_FACTOR)) {
				t.setBackground(Colors.forRequiredField());
			} else {
				t.setBackground(Colors.forModifiedDefault());
			}
		});
		if (!Num.equal(net().smoothingFactor, Defaults.SMOOTHING_FACTOR))
			t.setBackground(Colors.forModifiedDefault());
		HelpLink.create(comp, tk, "Glättungsfaktor", H.SmoothingFactor);
		Button reset = tk.createButton(comp, "Standardwert", SWT.NONE);
		reset.setToolTipText("Auf Standardwert zurücksetzen");
		Controls.onSelect(reset, e -> Texts.set(t, Defaults.SMOOTHING_FACTOR));
	}

	private double calculateMaxSimLoad() {
		HeatNet net = net();
		double max = net.maxLoad == null ? calculateMaxLoad() : net.maxLoad;
		double sim = net.simultaneityFactor * max;
		return Math.ceil(sim);
	}

	private double calculateMaxLoad() {
		try {
			// net load from the net-specification on this page
			double load = ProjectLoad.getNetLoad(net());
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
