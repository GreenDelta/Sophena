package sophena.rcp.editors.basedata.products;

import java.util.List;
import java.util.UUID;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ProductDao;
import sophena.model.Product;
import sophena.model.ProductType;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.Labels;
import sophena.rcp.M;
import sophena.rcp.Numbers;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;

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
		createProductSection(body, toolkit);
		form.reflow(true);
	}

	private void createProductSection(Composite parent, FormToolkit toolkit) {
		Section section = UI.section(parent, toolkit, Labels.get(type));
		UI.gridData(section, true, true);
		Composite comp = UI.sectionClient(section, toolkit);
		UI.gridLayout(comp, 1);
		TableViewer table = Tables.createViewer(comp, getColumns());
		table.setLabelProvider(new ProductLabel());
		table.setInput(products);
		double x = 1 / 4d;
		Tables.bindColumnWidths(table, x, x, x, x);
		bindProductActions(section, table);
	}

	private String[] getColumns() {

		return new String[] { "Bezeichnung", "Link", "Preis" };

	}

	private void bindProductActions(Section section, TableViewer table) {
		Action add = Actions.create(M.Add, Images.ADD_16.des(), () -> addProduct(table));
		Action edit = Actions.create(M.Edit, Images.EDIT_16.des(), () -> editProduct(table));
		Action del = Actions.create(M.Delete, Images.DELETE_16.des(), () -> deleteProduct(table));
		Actions.bind(section, add, edit, del);
		Actions.bind(table, add, edit, del);
		Tables.onDoubleClick(table, (e) -> editProduct(table));
	}

	private void addProduct(TableViewer table) {

		Product product = new Product();
		product.id = UUID.randomUUID().toString();
		product.name = Labels.get(type);
		product.url = "";
		product.purchasePrice = (double) 0;
		if (ProductWizard.open(product) != Window.OK)
			return;
		dao.insert(product);
		products.add(product);
		table.setInput(products);
	}

	private void editProduct(TableViewer table) {
		Product product = Viewers.getFirstSelected(table);
		if (product == null)
			return;
		if (ProductWizard.open(product) != Window.OK)
			return;
		try {
			int idx = products.indexOf(product);
			product = dao.update(product);
			products.set(idx, product);
			table.setInput(products);
		} catch (Exception e) {
			log.error("failed to update Product ", product, e);
		}
	}

	private void deleteProduct(TableViewer table) {
		Product product = Viewers.getFirstSelected(table);
		if (product == null)
			return;
		boolean doIt = MsgBox.ask(M.Delete, "Soll das ausgewählte Produkt wirklich gelöscht werden?");
		if (!doIt)
			return;
		try {
			dao.delete(product);
			products.remove(product);
			table.setInput(products);
		} catch (Exception e) {
			log.error("failed to delete Product " + product, e);
		}
	}

	private class ProductLabel extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int col) {
			if (col != 0)
				return null;
			return Images.PRODUCT_16.img();
		}

		@Override
		public String getColumnText(Object element, int col) {
			if (!(element instanceof Product))
				return null;
			Product product = (Product) element;
			switch (col) {
			case 0:
				return Labels.get(product.type);
			case 1:
				return product.name;
			case 2:
				return product.url;
			case 3:
				return Numbers.toString(product.purchasePrice) + " EUR";

			default:
				return null;
			}
		}
	}
}
