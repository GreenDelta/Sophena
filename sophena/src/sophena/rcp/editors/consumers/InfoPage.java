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
		FormToolkit tk = managedForm.getToolkit();
		Composite body = UI.formBody(form, tk);
		InfoSection.of(editor).create(body, tk);
		new LoadCurveSection(editor, body, tk);
		HeatDemandSection.of(editor).create(body, tk);
		if (!consumer().isDemandBased()) {
			ConsumptionSection.of(editor).create(body, tk);
			LoadProfileSection.of(editor).create(body, tk);
		}
		form.reflow(true);
	}
}
