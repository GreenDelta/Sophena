package sophena.rcp.editors.basedata;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.db.daos.RootEntityDao;
import sophena.model.ProductGroup;
import sophena.rcp.App;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.UI;

public class ProductGroupEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("data.product.groups",
				"Produktgruppen");
		Editors.open(input, "sophena.ProductGroupEditor");
	}

	@Override
	protected void addPages() {
		try {
			addPage(new Page());
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}

	private class Page extends FormPage {

		private RootEntityDao<ProductGroup> dao;
		private List<ProductGroup> groups;

		Page() {
			super(ProductGroupEditor.this, "ProductGroupPage",
					"Produktgruppen");
			dao = new RootEntityDao<>(ProductGroup.class, App.getDb());
			groups = dao.getAll();
		}

		@Override
		protected void createFormContent(IManagedForm managedForm) {
			ScrolledForm form = UI.formHeader(managedForm, "Produktgruppen");
			FormToolkit toolkit = managedForm.getToolkit();
			Composite body = UI.formBody(form, toolkit);
			// TODO: create page content
			form.reflow(true);
		}
	}
}
