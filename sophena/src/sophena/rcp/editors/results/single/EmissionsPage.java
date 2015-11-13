package sophena.rcp.editors.results.single;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.calc.EnergyResult;
import sophena.rcp.utils.UI;

class EmissionsPage extends FormPage {

	private EnergyResult result;

	EmissionsPage(ResultEditor editor) {
		super(editor, "sophena.EmissionsPage", "Emissionen und Verluste");
		this.result = editor.result.energyResult;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Emissionen und Verluste");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		Composite c1 = UI.formSection(body, tk, "Treibhausgasemissionen");
		EmissionTable.create(result, c1);
		Composite c2 = UI.formSection(body, tk, "Verluste");
		HeatLossTable.create(result, c2);
	}

}
