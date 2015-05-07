package sophena.rcp.editors.costs;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import sophena.rcp.utils.UI;

class OverviewPage extends FormPage {

	private CostEditor editor;

	public OverviewPage(CostEditor editor) {
		super(editor, "sophena.CostOverviewPage", "Kostenübersicht");
		this.editor = editor;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Kostenübersicht");
		FormToolkit toolkit = mform.getToolkit();
		Composite body = UI.formBody(form, toolkit);
		form.reflow(true);
	}
}
