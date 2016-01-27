package sophena.rcp.editors.results.single;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

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
		Composite c1 = UI.formSection(body, tk, "Treibhausgasemissionen");
		EmissionTable.create(editor.result.energyResult, c1);
		Composite c2 = UI.formSection(body, tk, "Verluste");
		HeatLossTable.create(editor, c2);
	}

}
