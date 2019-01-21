package sophena.rcp.editors.costs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.Labels;
import sophena.model.Product;
import sophena.model.ProductCosts;
import sophena.model.ProductEntry;
import sophena.model.ProductType;
import sophena.model.Project;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpBox;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Strings;

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
		fillEntries(); // to collapse the section if necessary
		Section section;
		if (entries.isEmpty()) {
			section = UI.collapsedSection(body, tk, Labels.getPlural(type));
		} else {
			section = UI.section(body, tk, Labels.getPlural(type));
		}
		Composite composite = UI.sectionClient(section, tk);
		table = createTable(composite);
		fillEntries(); // to fill the table
		Action info = Actions.create("Information",
				Icon.INFO_16.des(), this::info);
		Action addGlobal = Actions.create(
				"Produkt aus Produktdatenbank hinzufügen",
				Icon.SEARCH_16.des(), this::addGlobal);
		Action addPrivate = Actions.create("Neues Produkt erstellen",
				Icon.ADD_16.des(), this::addPrivate);
		Action edit = Actions.create(M.Edit, Icon.EDIT_16.des(), this::edit);
		Action del = Actions.create(M.Remove, Icon.DELETE_16.des(),
				this::delete);
		Actions.bind(section, addGlobal, addPrivate, edit, del, info);
		Actions.bind(table, addGlobal, addPrivate, edit, del);
		Tables.onDoubleClick(table, e -> edit());
	}

	private TableViewer createTable(Composite comp) {
		TableViewer table = Tables.createViewer(comp, "Produkt",
				"Anzahl", "Investitionskosten", "Nutzungsdauer",
				"Instandsetzung",
				"Wartung und Inspektion", "Aufwand für Bedienen");
		double w = 1d / 7d;
		Tables.bindColumnWidths(table, w, w, w, w, w, w, w);
		table.setLabelProvider(new EntryLabel(type));
		return table;
	}

	private void info() {
		switch (type) {
		case BOILER_ACCESSORIES:
			HelpBox.show("Kesselzubehör", H.BoilerAccessories);
			break;
		case BOILER_HOUSE_TECHNOLOGY:
			HelpBox.show("Heizhaus-Technik", H.BoilerHouseTechnology);
			break;
		case BUILDING:
			HelpBox.show("Gebäude", H.Buildings);
			break;
		case HEATING_NET_TECHNOLOGY:
			HelpBox.show("Wärmenetz-Technik", H.HeatingNetTechnology);
			break;
		case HEATING_NET_CONSTRUCTION:
			HelpBox.show("Wärmenetz-Bau", H.HeatingNetConstruction);
			break;
		case PLANNING:
			HelpBox.show("Planung", H.Planning);
			break;
		default:
			break;
		}
	}

	/** Add a product from the product database. */
	private void addGlobal() {
		ProductEntry entry = new ProductEntry();
		entry.id = UUID.randomUUID().toString();
		entry.costs = new ProductCosts();
		entry.count = 1;
		if (EntryWizard.open(entry, type, project().duration) != Window.OK)
			return;
		project().productEntries.add(entry);
		fillEntries();
		editor.setDirty();
	}

	/** Add a project-private product. */
	private void addPrivate() {
		ProductEntry entry = new ProductEntry();
		entry.id = UUID.randomUUID().toString();
		entry.costs = new ProductCosts();
		entry.count = 1;
		Product product = new Product();
		product.id = UUID.randomUUID().toString();
		product.projectId = project().id;
		product.type = type;
		entry.product = product;
		if (EntryWizard.open(entry, type, project().duration) != Window.OK)
			return;
		project().productEntries.add(entry);
		project().ownProducts.add(entry.product);
		fillEntries();
		editor.setDirty();
	}

	private void edit() {
		ProductEntry entry = Viewers.getFirstSelected(table);
		if (entry == null)
			return;
		ProductEntry clone = copy(entry); // copy allows cancel options
		if (EntryWizard.open(clone, type, project().duration) != Window.OK)
			return;
		ProductEntry managed = getJpaManaged(entry.id);
		if (managed == null)
			return;
		copy(clone, managed);
		fillEntries();
		editor.setDirty();
	}

	private ProductEntry copy(ProductEntry entry) {
		ProductEntry clone = entry.clone();
		if (clone.product != null && clone.product.projectId != null) {
			// clone the private product
			clone.product = entry.product.clone();
			clone.product.id = entry.product.id;
		}
		return clone;
	}

	private void copy(ProductEntry copy, ProductEntry managed) {
		managed.costs = copy.costs;
		managed.count = copy.count;
		managed.pricePerPiece = copy.pricePerPiece;
		if (copy.product == null || copy.product.projectId == null) {
			managed.product = copy.product;
			return;
		}
		if (managed.product == null)
			return;
		managed.product.name = copy.product.name;
		managed.product.group = copy.product.group;
	}

	private void delete() {
		ProductEntry entry = Viewers.getFirstSelected(table);
		if (entry == null)
			return;
		ProductEntry managed = getJpaManaged(entry.id);
		if (managed == null)
			return;
		project().productEntries.remove(managed);
		if (managed.product != null && managed.product.projectId != null)
			project().ownProducts.remove(managed.product);
		fillEntries();
		editor.setDirty();
	}

	private void fillEntries() {
		entries.clear();
		for (ProductEntry e : project().productEntries) {
			if (e.product != null && e.product.type == type) {
				entries.add(e);
			}
		}
		if (table != null) {
			table.setInput(entries);
		}
	}

	private ProductEntry getJpaManaged(String id) {
		for (ProductEntry e : project().productEntries) {
			if (Strings.nullOrEqual(id, e.id))
				return e;
		}
		return null;
	}

}
