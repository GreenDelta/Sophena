package sophena.rcp.editors.consumers;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import sophena.calc.ConsumerLoadCurve;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.utils.UI;

class HeatDemandSection {

	private ConsumerEditor editor;

	private HeatDemandSection(){
	}

	static HeatDemandSection of(ConsumerEditor editor) {
		HeatDemandSection section = new HeatDemandSection();
		section.editor = editor;
		return section;
	}

	void create(Composite body, FormToolkit tk) {
		Composite composite = UI.formSection(body, tk,  M.HeatDemand);
		Text totalPowerText = UI.formText(composite, tk, "#Gesamtleistung");
		Text heatDemandText = UI.formText(composite, tk, M.HeatDemand);
		Text loadHoursText = UI.formText(composite, tk, "#Vollaststunden");
		Text waterFractionText = UI.formText(composite, tk, "#Warmwasseranteil");
		Text heatLimitText = UI.formText(composite, tk, "#Heizgrenzleistung");
		UI.formLabel(composite, tk, "#Jahresdauerlinie");

		double[] result = ConsumerLoadCurve.calculate(editor.getConsumer(),
				editor.getProject().getWeatherStation(), App.getDb());
		for(double d : result)
			System.out.println(d);

	}

}
