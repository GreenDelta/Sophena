package sophena.rcp.editors.consumers;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.Consumer;
import sophena.rcp.M;
import sophena.rcp.Numbers;
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
		createHoursText(composite, tk);
		createHeatDemandText(composite, tk);
	}

	private void createPowerText(Composite composite, FormToolkit tk) {
		Text t = UI.formText(composite, tk, "#Gesamtleistung");
		t.setEditable(consumer().isDemandBased());
		t.setText(Numbers.toString(consumer().getHeatingLoad()));
		UI.formLabel(composite, tk, "kW");
		t.addModifyListener((e) -> {
			consumer().setHeatingLoad(Numbers.read(t.getText()));
			editor.updateLoadCurve();
			editor.setDirty();
		});
	}

	private void createWaterText(Composite composite, FormToolkit tk) {
		Text t = UI.formText(composite, tk, "#Warmwasseranteil");
		t.setText(Numbers.toString(consumer().getWaterFraction()));
		UI.formLabel(composite, tk, "%");
		t.addModifyListener((e) -> {
			consumer().setWaterFraction(Numbers.read(t.getText()));
			editor.updateLoadCurve();
			editor.setDirty();
		});
	}

	private void createHeatLimitText(Composite composite, FormToolkit tk) {
		Text t = UI.formText(composite, tk, "#Heizgrenze");
		t.setText(Numbers.toString(consumer().getHeatingLimit()));
		UI.formLabel(composite, tk, "°C");
		t.addModifyListener((e) -> {
			consumer().setHeatingLimit(Numbers.read(t.getText()));
			editor.updateLoadCurve();
			editor.setDirty();
		});
	}

	private void createHoursText(Composite composite, FormToolkit tk) {
		Text t = UI.formText(composite, tk, "#Vollaststunden");
		t.setText(Numbers.toString(consumer().getLoadHours()));
		UI.formLabel(composite, tk, "h");
		t.addModifyListener((e) -> {
			consumer().setLoadHours(Numbers.readInt(t.getText()));
			editor.setDirty();
		});
	}

	private void createHeatDemandText(Composite composite, FormToolkit tk) {
		Text t = UI.formText(composite, tk, M.HeatDemand);
		t.setEditable(false);
		UI.formLabel(composite, tk, "kWh");
	}

}
