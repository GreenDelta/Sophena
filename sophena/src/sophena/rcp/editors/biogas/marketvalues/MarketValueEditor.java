package sophena.rcp.editors.biogas.marketvalues;

import java.time.LocalDate;
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
import sophena.model.Stats;
import sophena.model.biogas.ElectricityMarketValue;
import sophena.rcp.M;
import sophena.rcp.app.App;
import sophena.rcp.app.Icon;
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

public class MarketValueEditor extends Editor {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public static void open() {
		var input = new KeyEditorInput("data.biogas.market.values", "Marktwerte");
		Editors.open(input, "sophena.MarketValueEditor");
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
		private final List<ElectricityMarketValue> values;

		Page() {
			super(MarketValueEditor.this, "MarketValuePage", "Marktwerte");
			values = new ArrayList<>(db.getAll(ElectricityMarketValue.class));
			Sorters.sortBaseData(values);
		}

		@Override
		protected void createFormContent(IManagedForm mForm) {
			var form = UI.formHeader(mForm, "Marktwerte");
			var tk = mForm.getToolkit();
			var body = UI.formBody(form, tk);
			createSection(body, tk);
			form.reflow(true);
		}

		private void createSection(Composite body, FormToolkit tk) {
			var section = UI.section(body, tk, "Marktwerte");
			UI.gridData(section, true, true);
			var comp = UI.sectionClient(section, tk);
			UI.gridLayout(comp, 1);
			var table = Tables.createViewer(comp,
				"Datensatz",
				"Jahresdurchschnitt",
				"Monatswerte");
			table.setLabelProvider(new TableLabel());
			table.setInput(values);
			Tables.bindColumnWidths(table, 0.35, 0.25, 0.4);
			bindActions(section, table);
		}

		private void bindActions(Section section, TableViewer table) {
			var add = Actions.create(M.Add, Icon.ADD_16.des(),
				() -> addValue(table));
			var edit = Actions.create(M.Edit, Icon.EDIT_16.des(),
				() -> editValue(table));
			var copy = Actions.create(M.Copy, Icon.COPY_16.des(),
				() -> copyValue(table));
			var delete = Actions.create(M.Delete, Icon.DELETE_16.des(),
				() -> deleteValue(table));
			Actions.bind(section, add, edit, copy, delete);
			Actions.bind(table, add, edit, copy, delete);
			Tables.onDoubleClick(table, _ -> editValue(table));
		}

		private void addValue(TableViewer table) {
			int year = LocalDate.now().getYear();
			var value = new ElectricityMarketValue();
			value.id = UUID.randomUUID().toString();
			value.name = "Marktwerte " + year;
			value.monthlyValues = new double[12];
			if (MarketValueWizard.open(value) != Window.OK)
				return;
			try {
				value = db.insert(value);
				values.add(value);
				table.setInput(values);
			} catch (Exception e) {
				log.error("failed to add market value {}", value, e);
			}
		}

		private void editValue(TableViewer table) {
			ElectricityMarketValue value = Viewers.getFirstSelected(table);
			if (value == null || MarketValueWizard.open(value) != Window.OK)
				return;
			try {
				int idx = values.indexOf(value);
				var next = db.update(value);
				if (idx >= 0) {
					values.set(idx, next);
					table.refresh();
				}
			} catch (Exception e) {
				log.error("failed to update market value {}", value, e);
			}
		}

		private void copyValue(TableViewer table) {
			ElectricityMarketValue value = Viewers.getFirstSelected(table);
			if (value == null)
				return;
			var copy = value.copy();
			copy.name = value.name + " - Kopie";
			copy.isProtected = false;
			if (MarketValueWizard.open(copy) != Window.OK)
				return;
			try {
				copy = db.insert(copy);
				values.add(copy);
				table.setInput(values);
			} catch (Exception e) {
				log.error("failed to copy market value {}", value, e);
			}
		}

		private void deleteValue(TableViewer table) {
			ElectricityMarketValue value = Viewers.getFirstSelected(table);
			if (value == null || value.isProtected)
				return;
			boolean doIt = MsgBox.ask("Löschen?",
				"Sollen die Marktwerte '" + value.name
					+ "' wirklich gelöscht werden?");
			if (!doIt)
				return;
			try {
				db.delete(value);
				values.remove(value);
				table.setInput(values);
			} catch (Exception e) {
				log.error("failed to delete market value {}", value, e);
			}
		}
	}

	private static class TableLabel extends BaseTableLabel {

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof ElectricityMarketValue value))
				return null;
			return switch (col) {
				case 0 -> value.name;
				case 1 -> Num.str(value.value) + " ct/kWh";
				case 2 -> monthlyRangeOf(value);
				default -> null;
			};
		}

		private String monthlyRangeOf(ElectricityMarketValue value) {
			double[] months = value.monthlyValues;
			if (months == null || months.length == 0)
				return "";
			return Num.str(Stats.min(months)) + " bis "
				+ Num.str(Stats.max(months)) + " ct/kWh";
		}
	}
}
