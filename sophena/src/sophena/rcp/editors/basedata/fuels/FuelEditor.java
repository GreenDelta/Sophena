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
import sophena.model.FuelGroup;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.editors.Editor;
import sophena.rcp.editors.basedata.UsageError;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;

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
				if (f.isWood())
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
					M.CalorificValue, "CO2 Emissionen",
					"Primärenergiefaktor", "Aschegehalt");
			table.setLabelProvider(TableLabel.getForNonWood());
			table.setInput(fuels);
			double w = 1d / 5d;
			Tables.bindColumnWidths(table, w, w, w, w, w);
			bindFuelActions(section, table);
		}

		private void bindFuelActions(Section section, TableViewer table) {
			Action add = Actions.create(M.Add, Icon.ADD_16.des(),
					() -> addFuel(table));
			Action edit = Actions.create(M.Edit, Icon.EDIT_16.des(),
					() -> edit(table, fuels, false));
			Action copy = Actions.create(M.Copy, Icon.COPY_16.des(),
					() -> copy(table, false));
			Action del = Actions.create(M.Delete, Icon.DELETE_16.des(),
					() -> delete(table, fuels, false));
			Actions.bind(section, add, edit, copy, del);
			Actions.bind(table, add, edit, copy, del);
			Tables.onDoubleClick(table, (e) -> edit(table, fuels, false));
		}

		private void addFuel(TableViewer table) {
			Fuel fuel = new Fuel();
			fuel.id = UUID.randomUUID().toString();
			fuel.name = M.Fuel;
			fuel.unit = "L";
			fuel.calorificValue = 10d;
			fuel.primaryEnergyFactor = 1d;
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

		private void copy(TableViewer table, boolean wood) {
			Fuel f = Viewers.getFirstSelected(table);
			if (f == null)
				return;
			Fuel copy = f.clone();
			copy.id = UUID.randomUUID().toString();
			copy.isProtected = false;
			int code = wood ? WoodFuelWizard.open(copy) : FuelWizard.open(copy);
			if (code != Window.OK)
				return;
			dao.insert(copy);
			if (wood) {
				woodFuels.add(copy);
				table.setInput(woodFuels);
			} else {
				fuels.add(copy);
				table.setInput(fuels);
			}
		}

		private void delete(TableViewer table, List<Fuel> list, boolean wood) {
			Fuel f = Viewers.getFirstSelected(table);
			if (f == null || f.isProtected)
				return;
			boolean doIt = MsgBox.ask(M.Delete,
					"Soll der ausgewählte Energieträger wirklich gelöscht werden?");
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

		private void createWoodSection(Composite body, FormToolkit tk) {
			Section section = UI.section(body, tk, M.WoodFuels);
			UI.gridData(section, true, true);
			Composite comp = UI.sectionClient(section, tk);
			UI.gridLayout(comp, 1);
			TableViewer table = Tables.createViewer(comp, M.WoodFuel,
					"Dichte", "Heizwert", "CO2 Emissionen",
					"Primärenergiefaktor", "Aschegehalt");
			table.setLabelProvider(TableLabel.getForWood());
			double w = 1d / 6d;
			Tables.bindColumnWidths(table, w, w, w, w, w, w);
			table.setInput(woodFuels);
			bindWoodActions(section, table);
		}

		private void bindWoodActions(Section section, TableViewer table) {
			Action add = Actions.create(M.Add, Icon.ADD_16.des(),
					() -> addWood(table));
			Action edit = Actions.create(M.Edit, Icon.EDIT_16.des(),
					() -> edit(table, woodFuels, true));
			Action copy = Actions.create(M.Copy, Icon.COPY_16.des(),
					() -> copy(table, true));
			Action del = Actions.create(M.Delete, Icon.DELETE_16.des(),
					() -> delete(table, woodFuels, true));
			Actions.bind(section, add, edit, copy, del);
			Actions.bind(table, add, edit, copy, del);
			Tables.onDoubleClick(table, (e) -> edit(table, woodFuels, true));
		}

		private void addWood(TableViewer table) {
			Fuel fuel = new Fuel();
			fuel.id = UUID.randomUUID().toString();
			fuel.name = M.WoodFuel;
			fuel.group = FuelGroup.WOOD;
			fuel.unit = "t atro";
			fuel.calorificValue = 5000d;
			fuel.density = 450d;
			fuel.group = FuelGroup.WOOD;
			fuel.primaryEnergyFactor = 0.2d;
			fuel.co2Emissions = 35d;
			fuel.ashContent = 1d;
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
	}
}
