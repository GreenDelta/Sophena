package sophena.rcp.editors.results.energy;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import sophena.calc.ProjectResult;
import sophena.rcp.utils.UI;

class EnergyResultPage extends FormPage {

	private ProjectResult result;

	public EnergyResultPage(EnergyResultEditor editor) {
		super(editor, "sophena.EnergyResultPage", "Ergebnisse");
		this.result = editor.getResult();
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Ergebnisse");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		BoilerSection boilerChart = new BoilerSection(body, tk);
		boilerChart.setResult(result);
	}
}
