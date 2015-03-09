package sophena.rcp.editors.consumers;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import sophena.rcp.M;
import sophena.rcp.utils.UI;

public class LocationPage extends FormPage {

	private ConsumerEditor editor;

	public LocationPage(ConsumerEditor editor) {
		super(editor, "sophena.LocationPage", M.Location);
		this.editor = editor;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, M.Location);
		FormToolkit toolkit = mform.getToolkit();
		Composite body = UI.formBody(form, toolkit);
		createAddressSection(body, toolkit);
		form.reflow(true);
	}

	private void createAddressSection(Composite body, FormToolkit toolkit) {
		Composite composite = UI.formSection(body, toolkit, M.Address);
		UI.formText(composite, toolkit, M.Street);
		UI.formText(composite, toolkit, M.ZipCode);
		UI.formText(composite, toolkit, M.City);
	}
}


