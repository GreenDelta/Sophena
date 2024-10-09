package sophena.rcp.editors.basedata.heatpumps;

import java.util.List;
import java.util.UUID;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import sophena.Labels;
import sophena.db.daos.RootEntityDao;
import sophena.db.usage.SearchResult;
import sophena.db.usage.UsageSearch;
import sophena.model.HeatPump;
import sophena.model.ProductType;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.editors.Editor;
import sophena.rcp.editors.basedata.BaseTableLabel;
import sophena.rcp.editors.basedata.ProductTables;
import sophena.rcp.editors.basedata.UsageError;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Num;

public class HeatPumpEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("data.heatPumps", M.HeatPump);
		Editors.open(input, "sophena.HeatPumpEditor");
	}

	@Override
	protected void addPages() {
		try {
			addPage(new Page());
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}

	class Page extends FormPage {

		private RootEntityDao<HeatPump> dao;
		private List<HeatPump> heatPumps;

		public Page() {
			super(HeatPumpEditor.this, "HeatPumpEditorPage", Labels.getPlural(ProductType.HEAT_PUMP));
			dao = new RootEntityDao<>(HeatPump.class, App.getDb());
			heatPumps = dao.getAll();
			Sorters.heatPumps(heatPumps);
		}

		@Override
		protected void createFormContent(IManagedForm managedForm) {
			ScrolledForm form = UI.formHeader(managedForm,
					Labels.getPlural(ProductType.HEAT_PUMP));
			FormToolkit toolkit = managedForm.getToolkit();
			Composite body = UI.formBody(form, toolkit);
			createSection(body, toolkit);
			form.reflow(true);
		}

		private void createSection(Composite parent, FormToolkit toolkit) {
			Section section = UI.section(parent, toolkit,
					Labels.getPlural(ProductType.HEAT_PUMP));
			UI.gridData(section, true, true);
			Composite comp = UI.sectionClient(section, toolkit);
			UI.gridLayout(comp, 1);
			TableViewer table = Tables.createViewer(comp, getColumns());
			table.setLabelProvider(new Label());
			table.setInput(heatPumps);
			double x = 1 / 4.0;
			Tables.bindColumnWidths(table, x, x, x, x);
			bindActions(section, table);
		}

		private String[] getColumns() {
				return new String[] { "Produktgruppe", "Bezeichnung", "Hersteller",
						"Minimale Leistung" };
		}

		private void bindActions(Section section, TableViewer table) {
			Action add = Actions.create(M.Add, Icon.ADD_16.des(),
					() -> add(table));
			Action edit = Actions.create(M.Edit, Icon.EDIT_16.des(),
					() -> edit(table));
			Action saveAs = Actions.create(M.Copy, Icon.COPY_16.des(),
					() -> saveAs(table));
			Action del = Actions.create(M.Delete, Icon.DELETE_16.des(),
					() -> delete(table));
			Actions.bind(section, add, edit, saveAs, del);
			Actions.bind(table, add, edit, saveAs, del);
			Tables.onDoubleClick(table, e -> edit(table));
		}

		private void add(TableViewer table) {
			HeatPump heatPump = new HeatPump();
			heatPump.type = ProductType.HEAT_PUMP;
			heatPump.id = UUID.randomUUID().toString();
			heatPump.name = Labels.get(ProductType.HEAT_PUMP) + " - neu";
			if (HeatPumpWizard.open(heatPump) != Window.OK)
				return;
			dao.insert(heatPump);
			heatPumps.add(heatPump);
			table.setInput(heatPump);
		}

		private void edit(TableViewer table) {
			HeatPump heatPump = Viewers.getFirstSelected(table);
			if (heatPump == null)
				return;
			if (HeatPumpWizard.open(heatPump) != Window.OK)
				return;
			try {
				int idx = heatPumps.indexOf(heatPump);
				heatPump = dao.update(heatPump);
				heatPumps.set(idx, heatPump);
				table.setInput(heatPumps);
			} catch (Exception e) {
				log.error("failed to update boiler ", heatPump, e);
			}
		}

		private void saveAs(TableViewer table) {
			HeatPump h = Viewers.getFirstSelected(table);
			if (h == null)
				return;
			HeatPump copy = h.copy();
			copy.id = UUID.randomUUID().toString();
			copy.isProtected = false;
			if (HeatPumpWizard.open(copy) != Window.OK)
				return;
			dao.insert(copy);
			heatPumps.add(copy);
			table.setInput(heatPumps);
		}

		private void delete(TableViewer table) {
			HeatPump heatPump = Viewers.getFirstSelected(table);
			if (heatPump == null || heatPump.isProtected)
				return;
			boolean doIt = MsgBox.ask(M.Delete,
					"Soll das ausgewählte Produkt wirklich gelöscht werden?");
			if (!doIt)
				return;
			List<SearchResult> usage = new UsageSearch(App.getDb()).of(heatPump);
			if (!usage.isEmpty()) {
				UsageError.show(usage);
				return;
			}
			try {
				dao.delete(heatPump);
				heatPumps.remove(heatPump);
				table.setInput(heatPumps);
			} catch (Exception e) {
				log.error("failed to delete heat pump " + heatPump, e);
			}
		}
	}

		private class Label extends BaseTableLabel {

			@Override
			public String getColumnText(Object obj, int col) {
				if (!(obj instanceof HeatPump))
					return null;
				HeatPump heatPump = (HeatPump) obj;
				if (col < 3)
					return ProductTables.getText(heatPump, col);
				switch (col) {
				case 3:
					return s(heatPump.minPower, "kW");
				default:
					return null;
				}
			}

			private String s(double val, String unit) {
				return Num.str(val) + " " + unit;
			}
		}
}
