package sophena.rcp.editors.consumers;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.rcp.M;
import sophena.rcp.utils.UI;

class LoadProfilesPage extends FormPage {

	private ConsumerEditor editor;

	public LoadProfilesPage(ConsumerEditor editor) {
		super(editor, "sophena.ConsumerLoadProfilesPage", M.LoadProfiles);
		this.editor = editor;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, M.LoadProfiles);
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		LoadProfileSection.of(editor).create(body, tk);
		form.reflow(true);
	}

}
