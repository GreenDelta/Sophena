package sophena.rcp.editors.results.single;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.calc.EnergyResult;
import sophena.calc.ProjectLoad;
import sophena.rcp.utils.UI;

class EnergyResultPage extends FormPage {

	private EnergyResult result;
	private double maxLoad;

	EnergyResultPage(ResultEditor editor) {
		super(editor, "sophena.EnergyResultPage", "Energie");
		this.result = editor.result.energyResult;
		this.maxLoad = ProjectLoad.getMax(editor.project);
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Ergebnisse - Energie");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		BoilerTableSection tableSection = new BoilerTableSection(result, maxLoad);
		tableSection.render(body, tk);
		BoilerChart unsortedChart = new BoilerChart(result, maxLoad);
		unsortedChart.setSorted(false);
		unsortedChart.render(body, tk);
		BoilerChart sortedChart = new BoilerChart(result, maxLoad);
		sortedChart.setSorted(true);
		sortedChart.render(body, tk);
		form.reflow(true);
	}
}
