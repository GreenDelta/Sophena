package sophena.rcp.editors.results;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.calc.EnergyResult;
import sophena.rcp.utils.UI;

class EnergyResultPage extends FormPage {

	private EnergyResult result;

	public EnergyResultPage(ResultEditor editor) {
		super(editor, "sophena.EnergyResultPage", "Energie");
		this.result = editor.result.energyResult;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Ergebnisse - Energie");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		BoilerTableSection tableSection = new BoilerTableSection(result);
		tableSection.render(body, tk);
		BoilerChart unsortedChart = new BoilerChart(result);
		unsortedChart.setSorted(false);
		unsortedChart.render(body, tk);
		BoilerChart sortedChart = new BoilerChart(result);
		sortedChart.setSorted(true);
		sortedChart.render(body, tk);
		form.reflow(true);
	}
}
