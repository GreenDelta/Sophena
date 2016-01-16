package sophena.rcp.editors.basedata.fuels;

import java.util.ArrayList;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.FuelDao;
import sophena.db.usage.SearchResult;
import sophena.db.usage.UsageSearch;
import sophena.model.Fuel;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.editors.Editor;
import sophena.rcp.editors.basedata.BaseTableLable;
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

public class FuelEditor extends Editor {

	private Logger log = LoggerFactory.getLogger(getClass());

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("data.fuels", M.Fuels);
		Editors.open(input, "sophena.FuelEditor");
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

		private FuelDao dao = new FuelDao(App.getDb());

		private List<Fuel> woodFuels = new ArrayList<>();
		private List<Fuel> fuels = new ArrayList<>();

		public Page() {
			super(FuelEditor.this, "FuelEditorPage", M.Fuels);
			initData();
		}

		private void initData() {
			List<Fuel> all = dao.getAll();
			Sorters.sortBaseData(all);
			for (Fuel f : all) {
				if (f.wood)
					woodFuels.add(f);
				else
					fuels.add(f);
			}
		}

		@Override
		protected void createFormContent(IManagedForm managedForm) {
			ScrolledForm form = UI.formHeader(managedForm, M.Fuels);
			FormToolkit toolkit = managedForm.getToolkit();
			Composite body = UI.formBody(form, toolkit);
			createFuelSection(body, toolkit);
			createWoodSection(body, toolkit);
			form.reflow(true);
		}

		private void createFuelSection(Composite body, FormToolkit toolkit) {
			Section section = UI.section(body, toolkit, M.Fuels);
			UI.gridData(section, true, true);
			Composite comp = UI.sectionClient(section, toolkit);
			UI.gridLayout(comp, 1);
			TableViewer table = Tables.createViewer(comp, M.Fuel,
					M.CalorificValue, "CO2 Emissionen");
			table.setLabelProvider(new FuelLabel());
			table.setInput(fuels);
			double w = 1d / 3d;
			Tables.bindColumnWidths(table, w, w, w);
			bindFuelActions(section, table);
		}

		private void bindFuelActions(Section section, TableViewer table) {
			Action add = Actions.create(M.Add, Images.ADD_16.des(),
					() -> addFuel(table));
			Action edit = Actions.create(M.Edit, Images.EDIT_16.des(),
					() -> edit(table, fuels, false));
			Action del = Actions.create(M.Delete, Images.DELETE_16.des(),
					() -> delete(table, fuels, false));
			Actions.bind(section, add, edit, del);
			Actions.bind(table, add, edit, del);
			Tables.onDoubleClick(table, (e) -> edit(table, fuels, false));
		}

		private void addFuel(TableViewer table) {
			Fuel fuel = new Fuel();
			fuel.id = UUID.randomUUID().toString();
			fuel.name = M.Fuel;
			fuel.unit = "L";
			fuel.calorificValue = (double) 10;
			fuel.wood = false;
			if (FuelWizard.open(fuel) != Window.OK)
				return;
			try {
				fuel = dao.insert(fuel);
				fuels.add(fuel);
				table.setInput(fuels);
			} catch (Exception e) {
				log.error("failed to add fuel " + fuel, e);
			}
		}

		private void edit(TableViewer table, List<Fuel> list, boolean wood) {
			Fuel f = Viewers.getFirstSelected(table);
			if (f == null)
				return;
			int code = wood ? WoodFuelWizard.open(f) : FuelWizard.open(f);
			if (code != Window.OK)
				return;
			try {
				int idx = list.indexOf(f);
				f = dao.update(f);
				list.set(idx, f);
				table.setInput(list);
			} catch (Exception e) {
				log.error("failed to update fuel " + f, e);
			}
		}

		private void delete(TableViewer table, List<Fuel> list, boolean wood) {
			Fuel f = Viewers.getFirstSelected(table);
			if (f == null || f.isProtected)
				return;
			boolean doIt = MsgBox.ask(M.Delete,
					"Soll der ausgewählte Brennstoff wirklich gelöscht werden?");
			if (!doIt)
				return;
			List<SearchResult> usage = new UsageSearch(App.getDb()).of(f);
			if (!usage.isEmpty()) {
				UsageError.show(usage);
				return;
			}
			try {
				dao.delete(f);
				list.remove(f);
				table.setInput(list);
			} catch (Exception e) {
				log.error("failed to delete fuel " + f, e);
			}
		}

		private void createWoodSection(Composite body, FormToolkit toolkit) {
			Section section = UI.section(body, toolkit, M.WoodFuels);
			UI.gridData(section, true, true);
			Composite comp = UI.sectionClient(section, toolkit);
			UI.gridLayout(comp, 1);
			TableViewer table = Tables.createViewer(comp, M.WoodFuel,
					"Dichte", "Heizwert", "CO2 Emissionen");
			table.setLabelProvider(new WoodLabel());
			double w = 1d / 4d;
			Tables.bindColumnWidths(table, w, w, w, w);
			table.setInput(woodFuels);
			bindWoodActions(section, table);
		}

		private void bindWoodActions(Section section, TableViewer table) {
			Action add = Actions.create(M.Add, Images.ADD_16.des(),
					() -> addWood(table));
			Action edit = Actions.create(M.Edit, Images.EDIT_16.des(),
					() -> edit(table, woodFuels, true));
			Action del = Actions.create(M.Delete, Images.DELETE_16.des(),
					() -> delete(table, woodFuels, true));
			Actions.bind(section, add, edit, del);
			Actions.bind(table, add, edit, del);
			Tables.onDoubleClick(table, (e) -> edit(table, woodFuels, true));
		}

		private void addWood(TableViewer table) {
			Fuel fuel = new Fuel();
			fuel.id = UUID.randomUUID().toString();
			fuel.name = M.WoodFuel;
			fuel.unit = "kg";
			fuel.calorificValue = (double) 5;
			fuel.density = (double) 450;
			fuel.wood = true;
			if (WoodFuelWizard.open(fuel) != Window.OK)
				return;
			try {
				fuel = dao.insert(fuel);
				woodFuels.add(fuel);
				table.setInput(woodFuels);
			} catch (Exception e) {
				log.error("failed to add fuel " + fuel, e);
			}
		}

		private class FuelLabel extends BaseTableLable {

			@Override
			public String getColumnText(Object element, int col) {
				if (!(element instanceof Fuel))
					return null;
				Fuel f = (Fuel) element;
				switch (col) {
				case 0:
					return f.name;
				case 1:
					return Num.str(f.calorificValue)
							+ " kWh/" + f.unit;
				case 2:
					return Num.str(f.co2Emissions) + " g CO2 äq./kWh";
				default:
					return null;
				}
			}
		}

		private class WoodLabel extends BaseTableLable {

			@Override
			public String getColumnText(Object element, int col) {
				if (!(element instanceof Fuel))
					return null;
				Fuel f = (Fuel) element;
				switch (col) {
				case 0:
					return f.name;
				case 1:
					return Num.str(f.density) + " kg/fm";
				case 2:
					return Num.str(f.calorificValue)
							+ " kWh/kg atro";
				case 3:
					return Num.str(f.co2Emissions) + " g CO2 äq./kWh";
				default:
					return null;
				}
			}
		}
	}
}
