package sophena.rcp.editors.producers;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import sophena.model.Producer;
import sophena.rcp.utils.UI;

public class InfoPage extends FormPage {

	private ProducerEditor editor;

	public InfoPage(ProducerEditor editor) {
		super(editor, "sophena.ProducerInfoPage", "Wärmeerzeuger");
		this.editor = editor;
	}

	private Producer producer() {
		return editor.getProducer();
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, producer().getName());
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
	}
}
