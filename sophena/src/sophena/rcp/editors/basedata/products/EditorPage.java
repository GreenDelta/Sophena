package sophena.rcp.editors.basedata.products;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ProductDao;
import sophena.model.Product;
import sophena.model.ProductType;
import sophena.rcp.App;
import sophena.rcp.Labels;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.UI;

class EditorPage extends FormPage {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ProductType type;
	private ProductDao dao;
	private List<Product> products;

	public EditorPage(Editor editor, ProductType type) {
		super(editor, "ProductEditorPage", Labels.get(type));
		this.type = type;
		dao = new ProductDao(App.getDb());
		products = dao.getAll(type);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = UI.formHeader(managedForm, Labels.get(type));
		FormToolkit toolkit = managedForm.getToolkit();
		Composite body = UI.formBody(form, toolkit);
		form.reflow(true);
	}

}
