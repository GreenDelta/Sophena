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

import sophena.Labels;
import sophena.db.daos.RootEntityDao;
import sophena.model.ProductGroup;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

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
			Sorters.productGroups(groups);
		}

		@Override
		protected void createFormContent(IManagedForm managedForm) {
			ScrolledForm form = UI.formHeader(managedForm, "Produktgruppen");
			FormToolkit toolkit = managedForm.getToolkit();
			Composite body = UI.formBody(form, toolkit);
			createProductSection(body, toolkit);
			form.reflow(true);

		}

		private void createProductSection(Composite parent,
				FormToolkit toolkit) {
			Section section = UI.section(parent, toolkit, "Produktgruppen");
			UI.gridData(section, true, true);
			Composite comp = UI.sectionClient(section, toolkit);
			UI.gridLayout(comp, 1);
			TableViewer table = Tables.createViewer(comp, "Produktbereich",
					"Produktgruppe", "Nutzungsdauer", "Instandsetzung",
					"Wartung und Inspektion", "Aufwand für Bedienung");
			table.setLabelProvider(new ProductGroupLabel());
			table.setInput(groups);
			double x = 1 / 6f;
			Tables.bindColumnWidths(table, x, x, x, x, x, x);
			Tables.rightAlignColumns(table, 2, 3, 4, 5);
		}

		private class ProductGroupLabel extends LabelProvider
				implements ITableLabelProvider {

			@Override
			public Image getColumnImage(Object obj, int col) {
				if (!(obj instanceof ProductGroup))
					return null;
				ProductGroup p = (ProductGroup) obj;
				if (col == 0)
					return p.index == 0 ? Icon.PRODUCT_16.img() : null;
				else
					return null;
			}

			@Override
			public String getColumnText(Object obj, int col) {
				if (!(obj instanceof ProductGroup))
					return null;
				ProductGroup p = (ProductGroup) obj;
				boolean isOther = p.name != null
						&& p.name.toLowerCase().contains("sonstig");
				switch (col) {
				case 0:
					return p.index == 0 ? Labels.getPlural(p.type) : null;
				case 1:
					return p.name;
				case 2:
					return p.duration != 0
							? Num.intStr(p.duration) + " Jahre"
							: isOther ? "-" : "Projektlaufzeit";
				case 3:
					return p.repair != 0
							? Num.str(p.repair, 1) + " %"
							: isOther ? "-" : "0 %";
				case 4:
					return p.maintenance != 0
							? Num.str(p.maintenance, 1) + " %"
							: isOther ? "-" : "0 %";
				case 5:
					return p.operation != 0
							? Num.intStr(p.operation) + " Stunden/Jahr"
							: isOther ? "-" : "0 Stunden/Jahr";
				default:
					return null;
				}
			}

		}
	}
}
