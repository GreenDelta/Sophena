package sophena.rcp.editors.biogas.plant.manager;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.model.biogas.BiogasPlant;
import sophena.rcp.M;
import sophena.rcp.app.App;
import sophena.rcp.app.Icon;
import sophena.rcp.editors.Editor;
import sophena.rcp.editors.basedata.BaseTableLabel;
import sophena.rcp.editors.biogas.plant.BiogasPlantEditor;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Num;

/// This is the general page for creating, editing and deleting biogas-plants.
public class BiogasPlantManager extends Editor {

	public static void open() {
		var input = new KeyEditorInput("data.biogas.plants", "Biogasanlagen");
		Editors.open(input, "sophena.BiogasPlantManager");
	}

	public static void createNew() {
		BiogasPlantWizard.open().ifPresent(BiogasPlantEditor::open);
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

		private final List<BiogasPlant> plants;
		private TableViewer table;

		Page() {
			super(BiogasPlantManager.this, "PlantEditorPage", "Biogasanlagen");
			plants = new ArrayList<>(App.getDb().getAll(BiogasPlant.class));
			Sorters.byName(plants);
			Runnable reload = this::reload;
			App.events().subscribe(BiogasPlant.class, reload);
			onClosed(() -> App.events().unsubscribe(reload));
		}

		@Override
		protected void createFormContent(IManagedForm mForm) {
			var form = UI.formHeader(mForm, "Biogasanlagen");
			var tk = mForm.getToolkit();
			var body = UI.formBody(form, tk);
			createSection(body, tk);
			form.reflow(true);
		}

		private void createSection(Composite body, FormToolkit tk) {
			var section = UI.section(body, tk, "Biogasanlagen");
			UI.gridData(section, true, true);
			var comp = UI.sectionClient(section, tk);
			UI.gridLayout(comp, 1);
			table = Tables.createViewer(comp,
					"Name",
					"Bemessungsleistung"
			);
			table.setLabelProvider(new TableLabel());
			table.setInput(plants);
			Tables.bindColumnWidths(table, 0.5, 0.5);
			bindActions(section, table);
		}

		private void bindActions(Section section, TableViewer table) {
			var add = Actions.create(M.Add, Icon.ADD_16.des(),
					() -> addPlant(table));
			var edit = Actions.create(M.Edit, Icon.EDIT_16.des(),
					() -> editPlant(table));
			var copy = Actions.create(M.Copy, Icon.COPY_16.des(),
					() -> copyPlant(table));
			var del = Actions.create(M.Delete, Icon.DELETE_16.des(),
					() -> deletePlant(table));
			Actions.bind(section, add, edit, copy, del);
			Actions.bind(table, add, edit, copy, del);
			Tables.onDoubleClick(table, (e) -> editPlant(table));
		}

		private void addPlant(TableViewer table) {
			var plant = BiogasPlantWizard.open().orElse(null);
			if (plant == null)
				return;
			plants.add(plant);
			table.setInput(plants);
			BiogasPlantEditor.open(plant);
		}

		private void editPlant(TableViewer table) {
			BiogasPlant plant = Viewers.getFirstSelected(table);
			if (plant == null)
				return;
			BiogasPlantEditor.open(plant);
		}

		private void copyPlant(TableViewer table) {
			var plant = getFreshSelected(table);
			if (plant == null)
				return;
			var copy = plant.copy();
			copy.name += " - Kopie";
			App.getDb().insert(copy);
			plants.add(copy);
			table.setInput(plants);
			BiogasPlantEditor.open(copy);
		}

		private void deletePlant(TableViewer table) {
			var plant = getFreshSelected(table);
			if (plant == null)
				return;
			App.getDb().delete(plant);
			plants.remove(plant);
			table.setInput(plants);
		}

		private BiogasPlant getFreshSelected(TableViewer table) {
			BiogasPlant plant = Viewers.getFirstSelected(table);
			return plant != null
					? App.getDb().get(BiogasPlant.class, plant.id)
					: null;
		}

		private void reload() {
			plants.clear();
			plants.addAll(App.getDb().getAll(BiogasPlant.class));
			Sorters.byName(plants);
			if (table != null && table.getControl() != null
					&& !table.getControl().isDisposed()) {
				table.setInput(plants);
				table.refresh();
			}
		}
	}

	static class TableLabel extends BaseTableLabel {

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof BiogasPlant p))
				return null;
			double ratedPower = p.totalElectricPower();
			String powerLabel = Num.str(ratedPower) + " kW el.";
			if (p.boilers.size() > 1) {
				powerLabel += " (" + p.boilers.size() + " Blöcke)";
			}
			return switch (col) {
				case 0 -> p.name;
				case 1 -> powerLabel;
				default -> null;
			};
		}
	}

}
