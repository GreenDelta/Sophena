package sophena.rcp.editors.biogas.plant;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.model.biogas.BiogasPlant;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.editors.Editor;
import sophena.rcp.editors.basedata.BaseTableLabel;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Num;

public class BiogasPlantTable extends Editor {

	public static void open() {
		var input = new KeyEditorInput("data.biogas.plants", "Biogasanlagen");
		Editors.open(input, "sophena.BiogasPlantTable");
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

		Page() {
			super(BiogasPlantTable.this, "PlantEditorPage", "Biogasanlagen");
			plants = new ArrayList<>(App.getDb().getAll(BiogasPlant.class));
			Sorters.byName(plants);
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
			var table = Tables.createViewer(comp,
					"Name",
					"Kessel",
					"Bemessungsleistung"
			);
			table.setLabelProvider(new TableLabel());
			table.setInput(plants);
			double w = 1d / 3d;
			Tables.bindColumnWidths(table, w, w, w);
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
	}

	static class TableLabel extends BaseTableLabel {

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof BiogasPlant p))
				return null;
			return switch (col) {
				case 0 -> p.name;
				case 1 -> p.product != null ? p.product.name : null;
				case 2 -> Num.str(p.ratedPower);
				default -> null;
			};
		}
	}

}
