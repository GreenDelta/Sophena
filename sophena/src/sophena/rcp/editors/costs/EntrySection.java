package sophena.rcp.editors.costs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import sophena.Labels;
import sophena.math.costs.FittingsCostSync;
import sophena.math.costs.FittingsCostSync.Mode;
import sophena.model.Product;
import sophena.model.ProductCosts;
import sophena.model.ProductEntry;
import sophena.model.ProductType;
import sophena.model.Project;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Strings;

/// A section to edit cost properties of a product entry.
class EntrySection {

	private final CostEditor editor;
	private final ProductType type;

	private TableViewer table;
	private final List<ProductEntry> entries = new ArrayList<>();

	EntrySection(CostEditor editor, ProductType type) {
		this.editor = editor;
		this.type = type;
	}

	private Project project() {
		return editor.getProject();
	}

	void create(Composite body, FormToolkit tk) {
		// select the matching entries and create the section
		fillEntries();
		var section = entries.isEmpty()
			? UI.collapsedSection(body, tk, Labels.getPlural(type))
			: UI.section(body, tk, Labels.getPlural(type));
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		table = createTable(comp);
		table.setInput(entries);

		if (type == ProductType.HEATING_NET_CONSTRUCTION) {
			var btn = new Button(comp, SWT.NONE);
			btn.setText("Formteile aktualisieren");
			btn.setImage(Icon.CALCULATE_16.img());
			Controls.onSelect(btn, $ -> {
				var b = MsgBox.ask(
					"Kosten für Formteile aktualisieren?",
					"Sollen die Kosten für Formteile neu aus den " +
						"Wohrleitungen abgeschätzt werden?"
				);
				if (!b) return;
				var res = FittingsCostSync.of(project(), App.getDb())
					.withUpdate(Mode.REPLACE)
					.run();
				if (res.isError()) {
					MsgBox.error("Unerwarteter Fehler", res.error());
				}
				fillEntries();
			});
		}

		// create and bind the actions
		var addGlobal = Actions.create(
			"Produkt aus Produktdatenbank hinzufügen",
			Icon.SEARCH_16.des(),
			this::addGlobal
		);
		var addPrivate = Actions.create(
			"Neues Produkt erstellen",
			Icon.ADD_16.des(),
			this::addPrivate
		);
		var edit = Actions.create(M.Edit, Icon.EDIT_16.des(), this::edit);
		var del = Actions.create(M.Remove, Icon.DELETE_16.des(), this::delete);
		Actions.bind(section, addGlobal, addPrivate, edit, del);
		Actions.bind(table, addGlobal, addPrivate, edit, del);
		Tables.onDoubleClick(table, e -> edit());
	}

	private TableViewer createTable(Composite comp) {
		var table = Tables.createViewer(
			comp,
			"Produkt",
			"Anzahl",
			"Investitionskosten",
			"Nutzungsdauer",
			"Instandsetzung",
			"Wartung und Inspektion",
			"Aufwand für Bedienen"
		);
		double w = 1d / 7d;
		Tables.bindColumnWidths(table, w, w, w, w, w, w, w);
		table.setLabelProvider(new EntryLabel(type));
		return table;
	}

	/// Add a product from the product database.
	private void addGlobal() {
		var e = new ProductEntry();
		e.id = UUID.randomUUID().toString();
		e.costs = new ProductCosts();
		e.count = 1;
		if (EntryWizard.open(e, type, project().duration) != Window.OK) {
			return;
		}
		project().productEntries.add(e);
		fillEntries();
		editor.setDirty();
	}

	/// Add a project-private product, that is a product that only exists
	/// in this project.
	private void addPrivate() {
		var p = new Product();
		p.id = UUID.randomUUID().toString();
		p.projectId = project().id;
		p.type = type;

		var e = new ProductEntry();
		e.id = UUID.randomUUID().toString();
		e.costs = new ProductCosts();
		e.count = 1;
		e.product = p;
		if (EntryWizard.open(e, type, project().duration) != Window.OK) {
			return;
		}

		project().productEntries.add(e);
		project().ownProducts.add(e.product);
		fillEntries();
		editor.setDirty();
	}

	/// Edit the currently selected product entry
	private void edit() {
		ProductEntry entry = Viewers.getFirstSelected(table);
		if (entry == null) {
			return;
		}

		// create a copy to allow to cancel it
		var clone = copy(entry);
		if (EntryWizard.open(clone, type, project().duration) != Window.OK) {
			return;
		}
		copyToManaged(clone);

		fillEntries();
		editor.setDirty();
	}

	private ProductEntry copy(ProductEntry entry) {
		var clone = entry.copy();
		if (clone.product != null && clone.product.projectId != null) {
			// clone the private product
			clone.product = entry.product.copy();
			clone.product.id = entry.product.id;
		}
		return clone;
	}

	/// Copy the values of the given copied entry to the JPA managed
	/// instance of that entry.
	private void copyToManaged(ProductEntry copy) {
		var man = getJpaManaged(copy.id);
		if (man == null) {
			return;
		}
		man.costs = copy.costs;
		man.count = copy.count;
		man.pricePerPiece = copy.pricePerPiece;

		// updating global products
		if (copy.product == null || copy.product.projectId == null) {
			man.product = copy.product;
			return;
		}

		// updating private products
		if (man.product == null) {
			return;
		}
		man.product.name = copy.product.name;
		man.product.group = copy.product.group;
	}

	private void delete() {
		ProductEntry entry = Viewers.getFirstSelected(table);
		if (entry == null) {
			return;
		}
		var man = getJpaManaged(entry.id);
		if (man == null) {
			return;
		}

		project().productEntries.remove(man);
		if (man.product != null && man.product.projectId != null) {
			project().ownProducts.remove(man.product);
		}
		fillEntries();
		editor.setDirty();
	}

	private void fillEntries() {
		entries.clear();
		for (var e : project().productEntries) {
			if (e.product != null && e.product.type == type) {
				entries.add(e);
			}
		}
		if (table != null) {
			table.setInput(entries);
		}
	}

	private ProductEntry getJpaManaged(String id) {
		for (var e : project().productEntries) {
			if (Strings.nullOrEqual(id, e.id)) {
				return e;
			}
		}
		return null;
	}
}
