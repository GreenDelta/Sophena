package sophena.rcp.editors.basedata.costs;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.model.CostSettings;
import sophena.rcp.editors.CostSettingsPanel;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.UI;

class CostSettingsPage extends FormPage {

	private Editor editor;
	private CostSettings costs;
	private FormToolkit toolkit;

	CostSettingsPage(Editor editor, CostSettings costs) {
		super(editor, "CostSettingsPage", "Basis-Kosteneinstellungen");
		this.editor = editor;
		this.costs = costs;
	}

	public CostSettings getCosts() {
		return costs;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		String title = "Basis-Kosteneinstellungen";
		ScrolledForm form = UI.formHeader(mform, title);
		toolkit = mform.getToolkit();
		Composite body = UI.formBody(form, toolkit);
		CostSettingsPanel panel = new CostSettingsPanel(editor, () -> costs);
		panel.isForProject = false;
		panel.render(toolkit, body);
		form.reflow(true);
	}

}
