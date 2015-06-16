package sophena.rcp.editors.results;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.rcp.utils.UI;

class CostResultPage extends FormPage {

	public CostResultPage(ResultEditor editor) {
		super(editor, "sophena.CostResultPage", "Kosten");
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Ergebnisse - Kosten");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);

		form.reflow(true);
	}

}
