package sophena.rcp.editors.costs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.model.ProductCosts;
import sophena.model.ProductEntry;
import sophena.model.ProductType;
import sophena.model.Project;
import sophena.rcp.Images;
import sophena.rcp.Labels;
import sophena.rcp.M;
import sophena.rcp.Numbers;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;

class EntrySection {

	private CostEditor editor;
	private ProductType type;

	private List<ProductEntry> entries = new ArrayList<>();

	EntrySection(CostEditor editor, ProductType type) {
		this.editor = editor;
		this.type = type;
		for (ProductEntry e : editor.getProject().productEntries) {
			if (e.product != null && e.product.type == type)
				entries.add(e);
		}
		Collections.sort(entries,
				(e1, e2) -> Strings.compare(e1.product.name, e2.product.name));
	}

	private Project project() {
		return editor.getProject();
	}

	void create(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, Labels.getPlural(type));
		Composite composite = UI.sectionClient(section, tk);
		TableViewer table = createTable(composite);
		Action add = Actions.create(M.Add, Images.ADD_16.des(), () -> {
		});
		Action remove = Actions.create(M.Remove, Images.DELETE_16.des(),
				() -> {
				});
		Action edit = Actions.create(M.Edit, Images.EDIT_16.des(),
				() -> {
				});
		Actions.bind(section, add, edit, remove);
		Actions.bind(table, add, edit, remove);
	}

	private TableViewer createTable(Composite comp) {
		TableViewer table = Tables.createViewer(comp, Labels.get(type),
				"Investitionskosten", "Nutzungsdauer", "Instandsetzung",
				"Wartung und Inspektion", "Aufwand für Bedienen");
		Tables.bindColumnWidths(table, 0.2, 0.16, 0.16, 0.16, 0.16, 0.16);
		table.setLabelProvider(new Label());
		table.setInput(entries);
		return table;
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return col == 0 ? Labels.getImage(type) : null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof ProductEntry))
				return null;
			ProductEntry e = (ProductEntry) obj;
			ProductCosts c = e.costs;
			if (c == null)
				return null;
			switch (col) {
			case 0:
				return e.product != null ? e.product.name : null;
			case 1:
				return Numbers.toString(c.investment) + " EUR";
			case 2:
				return Numbers.toString(c.duration) + " a";
			case 3:
				return Numbers.toString(c.repair) + " %";
			case 4:
				return Numbers.toString(c.maintenance) + " %";
			case 5:
				return Numbers.toString(c.operation) + " h/a";
			default:
				return null;
			}
		}
	}

}
