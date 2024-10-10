package sophena.rcp.editors.producers;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.HeatPumpMode;
import sophena.model.Producer;
import sophena.rcp.utils.UI;

public class HeatPumpSection {
	private ProducerEditor editor;
	private Text moduleCount;


	HeatPumpSection(ProducerEditor editor) {
		this.editor = editor;
	}

	private Producer producer() {
		return editor.getProducer();
	}
	
	void create(Composite body, FormToolkit tk) {
		if (producer().heatPump == null)
			return;
		Composite comp = UI.formSection(body, tk,
				"Wärmepumpenspezifikation");
		UI.gridLayout(comp, 4);		
		if(producer().heatPumpMode == null)
			producer().heatPumpMode = HeatPumpMode.OUTODOOR_TEMPERATURE_MODE;

	}
}
