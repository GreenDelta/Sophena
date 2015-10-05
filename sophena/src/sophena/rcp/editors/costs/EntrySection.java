package sophena.rcp.editors.costs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
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
import sophena.rcp.utils.Viewers;

class EntrySection {

	private CostEditor editor;
	private ProductType type;

	private TableViewer table;
	private List<ProductEntry> entries = new ArrayList<>();

	EntrySection(CostEditor editor, ProductType type) {
		this.editor = editor;
		this.type = type;
	}

	private Project project() {
		return editor.getProject();
	}

	void create(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, Labels.getPlural(type));
		Composite composite = UI.sectionClient(section, tk);
		table = createTable(composite);
		fillEntries();
		Action add = Actions.create(M.Add, Images.ADD_16.des(), this::add);
		Action edit = Actions.create(M.Edit, Images.EDIT_16.des(), this::edit);
		Action del = Actions.create(M.Remove, Images.DELETE_16.des(),
				this::delete);
		Actions.bind(section, add, edit, del);
		Actions.bind(table, add, edit, del);
		Tables.onDoubleClick(table, e -> edit());
	}

	private TableViewer createTable(Composite comp) {
		TableViewer table = Tables.createViewer(comp, Labels.get(type),
				"Anzahl", "Investitionskosten", "Nutzungsdauer", "Instandsetzung",
				"Wartung und Inspektion", "Aufwand für Bedienen");
		double w = 1d / 7d;
		Tables.bindColumnWidths(table, w, w, w, w, w, w, w);
		table.setLabelProvider(new Label());
		return table;
	}

	private void add() {
		ProductEntry entry = new ProductEntry();
		entry.id = UUID.randomUUID().toString();
		entry.costs = new ProductCosts();
		if (EntryWizard.open(entry, type) != Window.OK)
			return;
		project().productEntries.add(entry);
		fillEntries();
		editor.setDirty();
	}

	private void edit() {
		ProductEntry entry = Viewers.getFirstSelected(table);
		if (entry == null)
			return;
		ProductEntry clone = entry.clone();
		if (EntryWizard.open(clone, type) != Window.OK)
			return;
		ProductEntry managed = getJpaManaged(entry.id);
		if (managed == null)
			return;
		managed.costs = clone.costs;
		managed.product = clone.product;
		fillEntries();
		editor.setDirty();
	}

	private void delete() {
		ProductEntry entry = Viewers.getFirstSelected(table);
		if (entry == null)
			return;
		ProductEntry managed = getJpaManaged(entry.id);
		if (managed == null)
			return;
		project().productEntries.remove(managed);
		fillEntries();
		editor.setDirty();
	}

	private void fillEntries() {
		entries.clear();
		for (ProductEntry e : project().productEntries) {
			if (e.product != null && e.product.type == type)
				entries.add(e);
		}
		if (table != null)
			table.setInput(entries);
	}

	private ProductEntry getJpaManaged(String id) {
		for (ProductEntry e : project().productEntries) {
			if (Strings.nullOrEqual(id, e.id))
				return e;
		}
		return null;
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
				return Numbers.toString(e.count) + " Stück";
			case 2:
				return Numbers.toString(c.investment) + " EUR";
			case 3:
				return Numbers.toString(c.duration) + " a";
			case 4:
				return Numbers.toString(c.repair) + " %";
			case 5:
				return Numbers.toString(c.maintenance) + " %";
			case 6:
				return Numbers.toString(c.operation) + " h/a";
			default:
				return null;
			}
		}
	}

}
