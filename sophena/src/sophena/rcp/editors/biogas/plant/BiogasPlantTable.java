package sophena.rcp.editors.biogas.plant;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.db.Database;
import sophena.model.biogas.BiogasPlant;
import sophena.rcp.App;
import sophena.rcp.editors.Editor;
import sophena.rcp.editors.basedata.BaseTableLabel;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
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

		private final Database db = App.getDb();
		private final List<BiogasPlant> plants;

		Page() {
			super(BiogasPlantTable.this, "PlantEditorPage", "Biogasanlagen");
			plants = new ArrayList<>(db.getAll(BiogasPlant.class));
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
