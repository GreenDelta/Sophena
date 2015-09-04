package sophena.rcp.editors.basedata;

import java.util.Collections;
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
			Collections.sort(groups, (s1, s2) -> {
				if (s2.type == null || s1.type == null)
					return 0;
				if (s1.type != s2.type)
					return s1.type.ordinal() - s2.type.ordinal();
				return s1.index - s2.index;
			});
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
		}

		private class ProductGroupLabel extends LabelProvider
				implements ITableLabelProvider {

			@Override
			public Image getColumnImage(Object obj, int col) {
				if (!(obj instanceof ProductGroup))
					return null;
				ProductGroup p = (ProductGroup) obj;
				if (col == 0)
					return p.index == 1 ? Images.PRODUCT_16.img() : null;
				else
					return null;
			}

			@Override
			public String getColumnText(Object obj, int col) {
				if (!(obj instanceof ProductGroup))
					return null;
				ProductGroup p = (ProductGroup) obj;
				switch (col) {
				case 0:
					return p.index == 1 ? Labels.get(p.type) : null;
				case 1:
					return p.name;
				case 2:
					return Numbers.toString(p.duration) + " Jahre";
				case 3:
					return Numbers.toString(p.repair) + " %";
				case 4:
					return Numbers.toString(p.maintenance) + " %";
				case 5:
					return Numbers.toString(p.operation) + " Stunden/Jahr";
				default:
					return null;
				}
			}
		}
	}

}
