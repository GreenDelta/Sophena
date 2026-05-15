package sophena.rcp.editors.biogas.plant;

import java.util.List;
import java.util.UUID;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.rcp.M;
import sophena.rcp.app.Icon;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.BiogasPlantBoiler;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Lists;
import sophena.utils.Num;

class BiogasPlantBoilerTable {

	private final BiogasPlantEditor editor;
	private TableViewer table;

	private BiogasPlantBoilerTable(BiogasPlantEditor editor) {
		this.editor = editor;
	}

	static BiogasPlantBoilerTable of(BiogasPlantEditor editor) {
		return new BiogasPlantBoilerTable(editor);
	}

	private BiogasPlant plant() {
		return editor.plant();
	}

	void render(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "BHKW-Blöcke");
		Composite comp = UI.sectionClient(section, tk);
		table = Tables.createViewer(comp,
				"Produkt",
				"Hersteller",
				"Max. Wärmeleistung",
				"Max. el. Leistung",
				"Investition");

		Tables.bindColumnWidths(table, 0.30, 0.20, 0.18, 0.18, 0.14);
		table.setLabelProvider(new BoilerLabel());
		var add = Actions.create(M.Add, Icon.ADD_16.des(), this::add);
		var edit = Actions.create(M.Edit, Icon.EDIT_16.des(), this::edit);
		var remove = Actions.create(M.Remove, Icon.DELETE_16.des(), this::remove);
		Actions.bind(section, add, edit, remove);
		Actions.bind(table, add, edit, remove);
		Tables.onDoubleClick(table, e -> edit());
		Tables.onDeletePressed(table, e -> remove());
		table.setInput(plant().boilers);
	}

	private void add() {
		var entry = new BiogasPlantBoiler();
		entry.id = UUID.randomUUID().toString();
		if (BiogasPlantBoilerWizard.open(entry, plant().productGroup) != Window.OK)
			return;
		plant().boilers.add(entry);
		refresh();
	}

	private void edit() {
		BiogasPlantBoiler selected = Viewers.getFirstSelected(table);
		selected = Lists.find(selected, plant().boilers);
		if (selected == null)
			return;
		var clone = selected.copy();
		if (BiogasPlantBoilerWizard.open(clone, plant().productGroup) != Window.OK)
			return;
		selected.boiler = clone.boiler;
		selected.costs = clone.costs;
		refresh();
	}

	private void remove() {
		List<BiogasPlantBoiler> selected = Viewers.getAllSelected(table);
		selected = Lists.findAll(selected, plant().boilers);
		if (selected.isEmpty())
			return;
		plant().boilers.removeAll(selected);
		refresh();
	}

	private void refresh() {
		table.setInput(plant().boilers);
		editor.setDirty();
		editor.calculate();
	}

	private static class BoilerLabel extends LabelProvider
			implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof BiogasPlantBoiler entry) || entry.boiler == null)
				return null;
			return switch (col) {
				case 0 -> entry.boiler.name;
				case 1 -> entry.boiler.manufacturer != null
						? entry.boiler.manufacturer.name
						: null;
				case 2 -> Num.str(entry.boiler.maxPower) + " kW";
				case 3 -> Num.str(entry.boiler.maxPowerElectric) + " kW";
				case 4 -> entry.costs != null
						? Num.str(entry.costs.investment) + " EUR"
						: null;
				default -> null;
			};
		}
	}
}
