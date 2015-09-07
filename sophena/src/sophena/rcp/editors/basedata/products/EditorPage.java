package sophena.rcp.editors.basedata.products;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.model.ProductType;
import sophena.rcp.Labels;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.UI;

class EditorPage extends FormPage {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ProductType type;

	public EditorPage(Editor editor, ProductType type) {
		super(editor, "ProductEditorPage", Labels.get(type));
		this.type = type;
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = UI.formHeader(managedForm, Labels.get(type));
		FormToolkit toolkit = managedForm.getToolkit();
		Composite body = UI.formBody(form, toolkit);
		form.reflow(true);
	}

}
