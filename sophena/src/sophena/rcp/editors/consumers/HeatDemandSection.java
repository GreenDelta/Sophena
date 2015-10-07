package sophena.rcp.editors.consumers;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.Consumer;
import sophena.model.Stats;
import sophena.rcp.M;
import sophena.rcp.Numbers;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class HeatDemandSection {

	private ConsumerEditor editor;

	private HeatDemandSection() {
	}

	static HeatDemandSection of(ConsumerEditor editor) {
		HeatDemandSection section = new HeatDemandSection();
		section.editor = editor;
		return section;
	}

	private Consumer consumer() {
		return editor.getConsumer();
	}

	void create(Composite body, FormToolkit tk) {
		Composite composite = UI.formSection(body, tk, M.HeatDemand);
		UI.gridLayout(composite, 6);
		createPowerText(composite, tk);
		createWaterText(composite, tk);
		createHeatLimitText(composite, tk);
		createHeatDemandText(composite, tk);
	}

	private void createPowerText(Composite composite, FormToolkit tk) {
		Text t = UI.formText(composite, tk, "Heizlast");
		Texts.set(t, consumer().heatingLoad);
		UI.formLabel(composite, tk, "kW");
		if (!consumer().demandBased) {
			t.setEditable(false);
			editor.onCalculated((values) -> {
				double heatingLoad = Stats.sum(values) / consumer().loadHours;
				consumer().heatingLoad = heatingLoad;
				Texts.set(t, heatingLoad);
			});
		} else {
			Texts.on(t).required().decimal().onChanged((text) -> {
				consumer().heatingLoad = Numbers.read(text);
				editor.calculate();
				editor.setDirty();
			});
		}
	}

	private void createWaterText(Composite composite, FormToolkit tk) {
		Text t = UI.formText(composite, tk, "Warmwasseranteil");
		t.setText(Numbers.toString(consumer().waterFraction));
		UI.formLabel(composite, tk, "%");
		t.addModifyListener((e) -> {
			consumer().waterFraction = Numbers.read(t.getText());
			editor.calculate();
			editor.setDirty();
		});
	}

	private void createHeatLimitText(Composite composite, FormToolkit tk) {
		Text t = UI.formText(composite, tk, "Heizgrenze");
		t.setText(Numbers.toString(consumer().heatingLimit));
		UI.formLabel(composite, tk, "°C");
		t.addModifyListener((e) -> {
			consumer().heatingLimit = Numbers.read(t.getText());
			editor.calculate();
			editor.setDirty();
		});
	}

	private void createHeatDemandText(Composite composite, FormToolkit tk) {
		Text t = UI.formText(composite, tk, M.HeatDemand);
		t.setEditable(false);
		UI.formLabel(composite, tk, "kWh");
		editor.onCalculated((values) -> {
			double sum = Stats.sum(values);
			t.setText(Numbers.toString(sum));
		});
	}

}
