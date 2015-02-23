package sophena.rcp.editors.consumers;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.model.Consumer;
import sophena.rcp.M;
import sophena.rcp.utils.UI;

class InfoPage extends FormPage {

	private ConsumerEditor editor;

	public InfoPage(ConsumerEditor editor) {
		super(editor, "sophena.ConsumerInfoPage", M.ConsumerInformation);
		this.editor = editor;
	}

	private Consumer consumer() {
		return editor.getConsumer();
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = UI.formHeader(managedForm, consumer().getName());
		FormToolkit toolkit = managedForm.getToolkit();
		Composite body = UI.formBody(form, toolkit);
		InfoSection.of(editor).create(body, toolkit);
		createAddressSection(body, toolkit);
		ConsumptionSection.of(editor).create(body, toolkit);
		BaseLoadSection.of(editor).create(body, toolkit);
		LoadProfileSection.of(editor).create(body, toolkit);
		form.reflow(true);
	}

	private void createAddressSection(Composite body, FormToolkit toolkit) {
		Composite composite = UI.formSection(body, toolkit, M.Address);
		UI.formText(composite, toolkit, M.Street);
		UI.formText(composite, toolkit, M.ZipCode);
		UI.formText(composite, toolkit, M.City);
	}
}
