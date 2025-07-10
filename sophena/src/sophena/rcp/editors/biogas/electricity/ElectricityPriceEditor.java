package sophena.rcp.editors.biogas.electricity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.Database;
import sophena.model.biogas.ElectricityPriceCurve;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.editors.Editor;
import sophena.rcp.editors.basedata.BaseTableLabel;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Num;

public class ElectricityPriceEditor extends Editor {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public static void open() {
		var input = new KeyEditorInput(
				"data.biogas.electricity.prices", "Strompreiskurven");
		Editors.open(input, "sophena.ElectricityPriceEditor");
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
		private final List<ElectricityPriceCurve> curves;

		Page() {
			super(
					ElectricityPriceEditor.this,
					"ElectricityPricePage",
					"Strompreiskurven");
			curves = new ArrayList<>(db.getAll(ElectricityPriceCurve.class));
			Sorters.byName(curves);
		}

		@Override
		protected void createFormContent(IManagedForm mForm) {
			var form = UI.formHeader(mForm, "Strompreiskurven");
			var tk = mForm.getToolkit();
			var body = UI.formBody(form, tk);
			createSection(body, tk);
			form.reflow(true);
		}

		private void createSection(Composite body, FormToolkit tk) {
			var section = UI.section(body, tk, "Strompreiskurven");
			UI.gridData(section, true, true);
			var comp = UI.sectionClient(section, tk);
			UI.gridLayout(comp, 1);
			var table = Tables.createViewer(comp,
					"Name",
					"Beschreibung",
					"Min. Preis [ct/kWh]",
					"Max. Preis [ct/kWh]",
					"Ø Preis [ct/kWh]"
			);
			table.setLabelProvider(new TableLabel());
			table.setInput(curves);
			Tables.bindColumnWidths(table, 0.25, 0.35, 0.15, 0.15, 0.1);
			bindActions(section, table);
		}

		private void bindActions(Section section, TableViewer table) {
			var add = Actions.create(M.Add, Icon.ADD_16.des(),
					() -> addCurve(table));
			var edit = Actions.create(M.Edit, Icon.EDIT_16.des(),
					() -> editCurve(table));
			var copy = Actions.create(M.Copy, Icon.COPY_16.des(),
					() -> copyCurve(table));
			var delete = Actions.create(M.Delete, Icon.DELETE_16.des(),
					() -> deleteCurve(table));
			var export = Actions.create("Export", Icon.EXPORT_16.des(),
					() -> exportCurve(table));

			Actions.bind(section, add, edit, copy, delete, export);
			Actions.bind(table, add, edit, copy, delete);
			Tables.onDoubleClick(table, e -> editCurve(table));
		}

		private void addCurve(TableViewer table) {
			var curve = new ElectricityPriceCurve();
			curve.id = UUID.randomUUID().toString();
			var code = ElectricityPriceWizard.open(curve);
			if (code == Window.OK) {
				curves.add(curve);
				db.insert(curve);
				table.setInput(curves);
				setSaved();
			}
		}

		private void editCurve(TableViewer table) {
			ElectricityPriceCurve curve = Viewers.getFirstSelected(table);
			if (curve == null)
				return;
			var code = ElectricityPriceWizard.open(curve);
			if (code == Window.OK) {
				db.update(curve);
				table.refresh();
				setSaved();
			}
		}

		private void copyCurve(TableViewer table) {
			ElectricityPriceCurve curve = Viewers.getFirstSelected(table);
			if (curve == null)
				return;
			var copy = curve.copy();
			copy.name = curve.name + " - Kopie";
			var code = ElectricityPriceWizard.open(copy);
			if (code == Window.OK) {
				curves.add(copy);
				db.insert(copy);
				table.setInput(curves);
				setSaved();
			}
		}

		private void deleteCurve(TableViewer table) {
			ElectricityPriceCurve curve = Viewers.getFirstSelected(table);
			if (curve == null)
				return;

			/* TODO: we need to implement usage search!
			var usage = UsageSearch.of(curve, db);
			if (usage.isUsed()) {
				UsageError.show(curve.name, usage);
				return;
			}
			*/

			boolean b = MsgBox.ask("Löschen?",
					"Möchten Sie die Strompreiskurve '"
							+ curve.name + "' wirklich löschen?");
			if (!b)
				return;

			curves.remove(curve);
			db.delete(curve);
			table.setInput(curves);
			setSaved();
		}

		private void exportCurve(TableViewer table) {
			ElectricityPriceCurve curve = Viewers.getFirstSelected(table);
			if (curve == null)
				return;
			ElectricityPriceIO.write(curve);
		}

		private static class TableLabel extends BaseTableLabel {

			@Override
			public String getColumnText(Object obj, int col) {
				if (!(obj instanceof ElectricityPriceCurve curve))
					return null;
				return switch (col) {
					case 0 -> curve.name;
					case 1 -> curve.description;
					case 2 -> getMinPrice(curve);
					case 3 -> getMaxPrice(curve);
					case 4 -> getAvgPrice(curve);
					default -> null;
				};
			}

			private String getMinPrice(ElectricityPriceCurve curve) {
				if (curve.values == null || curve.values.length == 0)
					return "-";
				double min = Double.MAX_VALUE;
				for (double value : curve.values) {
					if (value < min) min = value;
				}
				return Num.str(min);
			}

			private String getMaxPrice(ElectricityPriceCurve curve) {
				if (curve.values == null || curve.values.length == 0)
					return "-";
				double max = Double.MIN_VALUE;
				for (double value : curve.values) {
					if (value > max) max = value;
				}
				return Num.str(max);
			}

			private String getAvgPrice(ElectricityPriceCurve curve) {
				if (curve.values == null || curve.values.length == 0)
					return "-";
				double sum = 0;
				for (double value : curve.values) {
					sum += value;
				}
				return Num.str(sum / curve.values.length);
			}
		}
	}
}
