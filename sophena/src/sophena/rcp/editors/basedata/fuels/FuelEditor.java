package sophena.rcp.editors.basedata.fuels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.FuelDao;
import sophena.model.Fuel;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.Numbers;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;

public class FuelEditor extends FormEditor {

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

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
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
			Collections.sort(all, (f1, f2) -> Strings.compare(
					f1.getName(), f2.getName()));
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
					M.CalorificValue);
			table.setLabelProvider(new FuelLabel());
			table.setInput(fuels);
			Tables.bindColumnWidths(table, 0.5, 0.5);
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
			fuel.setName(M.Fuel);
			fuel.setUnit("L");
			fuel.setCalorificValue(10);
			fuel.setWood(false);
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
			if (f == null)
				return;
			boolean doIt = MsgBox
					.ask(M.Delete,
							"Soll der ausgewählte Brennstoff wirklich gelöscht werden?");
			if (!doIt)
				return;
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
					"Dichte", "Heizwert");
			table.setLabelProvider(new WoodLabel());
			Tables.bindColumnWidths(table, 0.40, 0.30, 0.30);
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
			fuel.setName(M.WoodFuel);
			fuel.setUnit("kg");
			fuel.setCalorificValue(5);
			fuel.setDensity(450);
			fuel.setWood(true);
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

		private class FuelLabel extends LabelProvider implements
				ITableLabelProvider {

			@Override
			public Image getColumnImage(Object element, int col) {
				return col == 0 ? Images.FUEL_16.img() : null;
			}

			@Override
			public String getColumnText(Object element, int col) {
				if (!(element instanceof Fuel))
					return null;
				Fuel f = (Fuel) element;
				switch (col) {
				case 0:
					return f.getName();
				case 1:
					return Numbers.toString(f.getCalorificValue())
							+ " kWh/" + f.getUnit();
				default:
					return null;
				}
			}
		}

		private class WoodLabel extends LabelProvider implements
				ITableLabelProvider {

			@Override
			public Image getColumnImage(Object element, int col) {
				return col == 0 ? Images.FUEL_16.img() : null;
			}

			@Override
			public String getColumnText(Object element, int col) {
				if (!(element instanceof Fuel))
					return null;
				Fuel f = (Fuel) element;
				switch (col) {
				case 0:
					return f.getName();
				case 1:
					return Numbers.toString(f.getDensity()) + " kg/fm";
				case 2:
					return Numbers.toString(f.getCalorificValue())
							+ "kWh/kg atro";
				default:
					return null;
				}
			}
		}
	}
}
