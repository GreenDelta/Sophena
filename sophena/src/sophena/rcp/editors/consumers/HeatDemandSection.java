package sophena.rcp.editors.consumers;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.BuildingState;
import sophena.model.Consumer;
import sophena.rcp.M;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpLink;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class HeatDemandSection {

	private ConsumerEditor editor;

	private Text heatingLimitText;
	private Text waterFractionText;
	private Text loadHoursText;

	private HeatDemandSection() {
	}

	static HeatDemandSection of(ConsumerEditor editor) {
		HeatDemandSection section = new HeatDemandSection();
		section.editor = editor;
		return section;
	}

	private Consumer consumer() {
		return editor.consumer;
	}

	void updateBuildingState(BuildingState state) {
		if (state == null)
			return;
		Texts.set(heatingLimitText, state.heatingLimit);
		Texts.set(waterFractionText, state.waterFraction);
		Texts.set(loadHoursText, state.loadHours);
	}

	HeatDemandSection create(Composite body, FormToolkit tk) {
		Composite composite = UI.formSection(body, tk, M.HeatDemand);
		UI.gridLayout(composite, 7);
		createHeatingLoadText(composite, tk);
		createWaterText(composite, tk);
		createHeatLimitText(composite, tk);
		createLoadHoursText(composite, tk);
		createHeatDemandText(composite, tk);
		return this;
	}

	private void createHeatingLoadText(Composite composite, FormToolkit tk) {
		Text t = UI.formText(composite, tk, "Heizlast");
		Texts.set(t, Num.intStr(consumer().heatingLoad));
		UI.formLabel(composite, tk, "kW");
		HelpLink.create(composite, tk, "Heizlast", H.HeatingLoad);
		if (!consumer().demandBased) {
			Texts.on(t).calculated();
			editor.onCalculated((profile, totals, total) -> {
				double heatingLoad = total / consumer().loadHours;
				consumer().heatingLoad = heatingLoad;
				Texts.set(t, Num.intStr(heatingLoad));
			});
		} else {
			Texts.on(t).required().decimal().onChanged(text -> {
				consumer().heatingLoad = Num.read(text);
				editor.calculate();
				editor.setDirty();
			});
		}
	}

	private void createWaterText(Composite composite, FormToolkit tk) {
		Text t = UI.formText(composite, tk, "Warmwasseranteil");
		Texts.set(t, consumer().waterFraction);
		UI.formLabel(composite, tk, "%");
		t.addModifyListener(e -> {
			consumer().waterFraction = Num.read(t.getText());
			editor.calculate();
			editor.setDirty();
		});
		waterFractionText = t;
	}

	private void createHeatLimitText(Composite composite, FormToolkit tk) {
		Text t = UI.formText(composite, tk, "Heizgrenze");
		Texts.set(t, consumer().heatingLimit);
		UI.formLabel(composite, tk, "°C");
		t.addModifyListener(e -> {
			consumer().heatingLimit = Num.read(t.getText());
			editor.calculate();
			editor.setDirty();
		});
		heatingLimitText = t;
		UI.filler(composite);
	}

	private void createLoadHoursText(Composite composite, FormToolkit tk) {
		Text t = UI.formText(composite, tk, "Volllaststunden");
		UI.formLabel(composite, tk, "h");
		Texts.on(t).integer().init(consumer().loadHours).onChanged(s -> {
			consumer().loadHours = Num.readInt(s);
			editor.calculate();
			editor.setDirty();
		});
		loadHoursText = t;
	}

	private void createHeatDemandText(Composite composite, FormToolkit tk) {
		Text t = UI.formText(composite, tk, M.HeatDemand);
		UI.formLabel(composite, tk, "kWh");
		Texts.on(t).calculated();
		editor.onCalculated((profile, totals, total) -> {
			t.setText(Num.intStr(total));
		});
	}

}
