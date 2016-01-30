package sophena.rcp.editors.results.single;

import java.util.function.Function;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.math.energetic.CO2Emissions;
import sophena.math.energetic.EfficiencyResult;
import sophena.rcp.M;
import sophena.rcp.utils.UI;

class FurtherResultsPage extends FormPage {

	private ResultEditor editor;

	FurtherResultsPage(ResultEditor editor) {
		super(editor, "sophena.EmissionsPage", M.FurtherResults);
		this.editor = editor;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, M.FurtherResults);
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		Function<String, Composite> s = title -> UI.formSection(body, tk, title);
		CO2Emissions emissions = CO2Emissions.calculate(editor.result);
		EmissionTable.create(emissions, s.apply("Treibhausgasemissionen"));
		EmissionChart.create(emissions, s.apply("Vergleich Treibhausgasemissionen"));
		EfficiencyResult efficiency = EfficiencyResult.calculate(editor.result);
		EfficiencyTable.create(efficiency, s.apply("Effizienz"));
		form.reflow(true);
	}

}
