package sophena.rcp.editors.biogas.plant;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.model.AnnualCostEntry;
import sophena.model.biogas.BiogasPlant;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.rcp.wizards.SimpleWizard;
import sophena.utils.Num;
import sophena.utils.Strings;

class BiogasAnnualCostsTable {

	private final BiogasPlantEditor editor;
	private TableViewer table;

	private BiogasAnnualCostsTable(BiogasPlantEditor editor) {
		this.editor = editor;
	}

	static BiogasAnnualCostsTable of(BiogasPlantEditor editor) {
		return new BiogasAnnualCostsTable(editor);
	}

	private BiogasPlant plant() {
		return editor.plant();
	}

	void render(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "Weitere sonstige Kosten");
		Composite comp = UI.sectionClient(section, tk);
		table = Tables.createViewer(comp, "Bezeichnung", "Jährliche Kosten");
		table.setLabelProvider(new EntryLabel());
		Tables.bindColumnWidths(table, 0.4, 0.3);
		Action add = Actions.create("Kostenpunkt hinzufügen",
				Icon.ADD_16.des(), this::add);
		Action edit = Actions.create(M.Edit, Icon.EDIT_16.des(), this::edit);
		Action del = Actions.create(M.Remove, Icon.DELETE_16.des(),
				this::delete);
		Actions.bind(section, add, edit, del);
		Actions.bind(table, add, edit, del);
		Tables.onDoubleClick(table, e -> edit());
		table.setInput(plant().otherAnnualCosts);
	}

	private void add() {
		AnnualCostEntry e = new AnnualCostEntry();
		Wizard wizard = new Wizard(e);
		if (wizard.open() != Window.OK)
			return;
		plant().otherAnnualCosts.add(e);
		table.setInput(plant().otherAnnualCosts);
		editor.setDirty();
	}

	private void edit() {
		AnnualCostEntry e = Viewers.getFirstSelected(table);
		if (e == null)
			return;
		AnnualCostEntry clone = e.copy();
		Wizard wizard = new Wizard(clone);
		if (wizard.open() != Window.OK)
			return;
		AnnualCostEntry managed = getJpaManaged(e);
		if (managed == null)
			return;
		managed.label = clone.label;
		managed.value = clone.value;
		table.setInput(plant().otherAnnualCosts);
		editor.setDirty();
	}

	private void delete() {
		AnnualCostEntry e = Viewers.getFirstSelected(table);
		if (e == null)
			return;
		AnnualCostEntry managed = getJpaManaged(e);
		if (managed == null)
			return;
		plant().otherAnnualCosts.remove(managed);
		table.setInput(plant().otherAnnualCosts);
		editor.setDirty();
	}

	private AnnualCostEntry getJpaManaged(AnnualCostEntry e) {
		for (AnnualCostEntry managed : plant().otherAnnualCosts) {
			if (!Strings.nullOrEqual(managed.label, e.label))
				continue;
			if (Double.compare(managed.value, e.value) == 0)
				return managed;
		}
		return null;
	}

	private class Wizard extends SimpleWizard {

		final AnnualCostEntry entry;
		private Text labelText;
		private Text valueText;

		Wizard(AnnualCostEntry entry) {
			super("Jährliche Kosten");
			this.entry = entry;
		}

		@Override
		protected void create(Composite comp) {
			UI.gridLayout(comp, 3);
			labelText = UI.formText(comp, "Bezeichnung");
			Texts.set(labelText, entry.label);
			UI.filler(comp);
			valueText = UI.formText(comp, "Wert");
			Texts.on(valueText).init(entry.value).decimal().required();
			UI.formLabel(comp, "EUR");
		}

		@Override
		protected boolean onFinish() {
			entry.label = labelText.getText();
			entry.value = Texts.getDouble(valueText);
			return true;
		}
	}

	private static class EntryLabel extends LabelProvider
			implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof AnnualCostEntry e))
				return null;
			return switch (col) {
				case 0 -> e.label;
				case 1 -> Num.str(e.value);
				default -> null;
			};
		}
	}
}
