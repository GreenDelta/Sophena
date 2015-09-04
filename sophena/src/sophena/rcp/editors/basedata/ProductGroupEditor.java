package sophena.rcp.editors.basedata;

import java.util.List;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import sophena.db.daos.RootEntityDao;
import sophena.model.ProductGroup;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.Labels;
import sophena.rcp.Numbers;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;

public class ProductGroupEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("data.product.groups", "Produktgruppen");
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
			super(ProductGroupEditor.this, "ProductGroupPage", "Produktgruppen");
			dao = new RootEntityDao<>(ProductGroup.class, App.getDb());
			groups = dao.getAll();
		}

		@Override
		protected void createFormContent(IManagedForm managedForm) {
			ScrolledForm form = UI.formHeader(managedForm, "Produktgruppen");
			FormToolkit toolkit = managedForm.getToolkit();
			Composite body = UI.formBody(form, toolkit);
			createProductSection(body, toolkit);
			// TODO: create page content
			form.reflow(true);

		}

		private void createProductSection(Composite parent, FormToolkit toolkit) {
			Section section = UI.section(parent, toolkit, "Produktgruppen");
			UI.gridData(section, true, true);
			Composite comp = UI.sectionClient(section, toolkit);
			UI.gridLayout(comp, 1);
			TableViewer table = Tables.createViewer(comp, "typen", "duration", "repair", "maintenance", "operation");
			table.setLabelProvider(new ProductGroupLabel());
			table.setInput(groups);
			double x = 1 / 5f;
			Tables.bindColumnWidths(table, x, x, x, x, x);

		}

		private class ProductGroupLabel extends LabelProvider implements ITableLabelProvider {

			@Override
			public Image getColumnImage(Object element, int col) {
				return col == 0 ? Images.PRODUCT_16.img() : null;
			}

			@Override
			public String getColumnText(Object element, int col) {
				if (!(element instanceof ProductGroup))
					return null;
				ProductGroup p = (ProductGroup) element;
				switch (col) {
				case 0:
					return getTypeLabel(p);// p.index == 0 ? getTypeLabel(p) :
											// null;
				case 1:
					return p.name;
				case 2:
					return getDuration(p);
				case 3:
					return getRepair(p);
				case 4:
					return getMaintenance(p);
				case 5:
					return getOperation(p);
				default:
					return null;
				}
			}

			private String getTypeLabel(ProductGroup p) {
				if (p.type == null)
					return null;
				else
					return Labels.get(p.type);
			}

			private String getDuration(ProductGroup p) {
				if (p == null)
					return null;
				else
					return Numbers.toString(p.duration) + "years";
			}

			private String getRepair(ProductGroup p) {
				if (p == null)
					return null;
				return Numbers.toString(p.repair) + "%";
			}

			private String getMaintenance(ProductGroup p) {
				if (p == null)
					return null;
				else
					return Numbers.toString(p.maintenance) + "%";

			}

			private String getOperation(ProductGroup p) {
				if (p == null)
					return null;
				else
					return Numbers.toString(p.operation) + "h/y";
			}

		}
	}

}
