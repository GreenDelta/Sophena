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
import sophena.db.usage.SearchResult;
import sophena.db.usage.UsageSearch;
import sophena.model.Product;
import sophena.model.ProductType;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.Labels;
import sophena.rcp.M;
import sophena.rcp.editors.Editor;
import sophena.rcp.editors.basedata.UsageError;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Num;

class EditorPage extends FormPage {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ProductType type;
	private ProductDao dao;
	private List<Product> products;

	public EditorPage(Editor editor, ProductType type) {
		super(editor, "ProductEditorPage", Labels.get(type));
		this.type = type;
		dao = new ProductDao(App.getDb());
		products = dao.getAllGlobal(type);
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
		TableViewer table = Tables.createViewer(comp, "Bezeichnung",
				"Produktgruppe", "Link", "Preis");
		table.setLabelProvider(new ProductLabel());
		table.setInput(products);
		double x = 1 / 4d;
		Tables.bindColumnWidths(table, x, x, x, x);
		bindProductActions(section, table);
	}

	private void bindProductActions(Section section, TableViewer table) {
		Action add = Actions.create(M.Add, Icon.ADD_16.des(),
				() -> addProduct(table));
		Action edit = Actions.create(M.Edit, Icon.EDIT_16.des(),
				() -> editProduct(table));
		Action saveAs = Actions.create(M.SaveAs, Icon.SAVE_AS_16.des(),
				() -> saveAs(table));
		Action del = Actions.create(M.Delete, Icon.DELETE_16.des(),
				() -> deleteProduct(table));
		Actions.bind(section, add, edit, saveAs, del);
		Actions.bind(table, add, edit, saveAs, del);
		Tables.onDoubleClick(table, e -> editProduct(table));
	}

	private void addProduct(TableViewer table) {
		Product product = new Product();
		product.id = UUID.randomUUID().toString();
		product.name = Labels.get(type);
		product.type = type;
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

	private void saveAs(TableViewer table) {
		Product p = Viewers.getFirstSelected(table);
		if (b == null)
			return;
		Product copy = b.clone();
		copy.id = UUID.randomUUID().toString();
		copy.isProtected = false;
		if (ProductWizard.open(copy) != Window.OK)
			return;
		dao.insert(copy);
		products.add(copy);
		table.setInput(products);
	}

	private void deleteProduct(TableViewer table) {
		Product product = Viewers.getFirstSelected(table);
		if (product == null)
			return;
		boolean doIt = MsgBox.ask(M.Delete,
				"Soll das ausgewählte Produkt wirklich gelöscht werden?");
		if (!doIt)
			return;
		List<SearchResult> usage = new UsageSearch(App.getDb()).of(product);
		if (!usage.isEmpty()) {
			UsageError.show(usage);
			return;
		}
		try {
			dao.delete(product);
			products.remove(product);
			table.setInput(products);
		} catch (Exception e) {
			log.error("failed to delete Product " + product, e);
		}
	}

	private class ProductLabel extends LabelProvider
			implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int col) {
			if (col != 0)
				return null;
			return Icon.PRODUCT_16.img();
		}

		@Override
		public String getColumnText(Object element, int col) {
			if (!(element instanceof Product))
				return null;
			Product product = (Product) element;
			switch (col) {
			case 0:
				return product.name;
			case 1:
				return product.group != null ? product.group.name : null;
			case 2:
				return product.url;
			case 3:
				return Num.str(product.purchasePrice) + " EUR";
			default:
				return null;
			}
		}
	}
}
