package sophena.rcp.editors.basedata.products;

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

import sophena.db.daos.RootEntityDao;
import sophena.db.usage.SearchResult;
import sophena.db.usage.UsageSearch;
import sophena.model.FlueGasCleaning;
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

public class FlueGasCleaningEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("flue.gas.cleaning.products",
				"Rauchgasreinigung");
		Editors.open(input, "sophena.products.FlueGasCleaningEditor");
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

		RootEntityDao<FlueGasCleaning> dao;
		List<FlueGasCleaning> cleanings;

		Page() {
			super(FlueGasCleaningEditor.this, "FlueGasCleaningPage",
					"Rauchgasreinigungsanlagen");
			dao = new RootEntityDao<>(FlueGasCleaning.class, App.getDb());
			cleanings = dao.getAll();
			Sorters.byName(cleanings);
		}

		@Override
		protected void createFormContent(IManagedForm managedForm) {
			ScrolledForm form = UI.formHeader(managedForm, "Rauchgasreinigungsanlagen");
			FormToolkit tk = managedForm.getToolkit();
			Composite body = UI.formBody(form, tk);
			createSection(body, tk);
			form.reflow(true);
		}

		private void createSection(Composite body, FormToolkit toolkit) {
			Section section = UI.section(body, toolkit, "Rauchgasreinigungsanlagen");
			UI.gridData(section, true, true);
			Composite comp = UI.sectionClient(section, toolkit);
			UI.gridLayout(comp, 1);
			TableViewer table = Tables.createViewer(comp, "Produktgruppe", "Bezeichnung", "Hersteller",
					"Brennstoff (Erz.)", "Max. Leistung (Erz.)", "Max. Volumenstrom");
			table.setLabelProvider(new Label());
			table.setInput(cleanings);
			double x = 1.0 / 6.0;
			Tables.bindColumnWidths(table, x, x, x, x, x, x);
			bindActions(section, table);
		}

		private void bindActions(Section section, TableViewer table) {
			Action add = Actions.create(M.Add, Icon.ADD_16.des(),
					() -> add(table));
			Action edit = Actions.create(M.Edit, Icon.EDIT_16.des(),
					() -> edit(table));
			Action del = Actions.create(M.Delete, Icon.DELETE_16.des(),
					() -> delete(table));
			Actions.bind(section, add, edit, del);
			Actions.bind(table, add, edit, del);
			Tables.onDoubleClick(table, (e) -> edit(table));
		}

		private void add(TableViewer table) {
			FlueGasCleaning c = new FlueGasCleaning();
			c.id = UUID.randomUUID().toString();
			c.type = ProductType.FLUE_GAS_CLEANING;
			c.name = "Neue Rauchgasreinigung";
			if (FlueGasCleaningWizard.open(c) != Window.OK)
				return;
			dao.insert(c);
			cleanings.add(c);
			table.setInput(cleanings);
		}

		private void edit(TableViewer table) {
			FlueGasCleaning c = Viewers.getFirstSelected(table);
			if (c == null)
				return;
			if (FlueGasCleaningWizard.open(c) != Window.OK)
				return;
			try {
				int idx = cleanings.indexOf(c);
				c = dao.update(c);
				cleanings.set(idx, c);
				table.setInput(cleanings);
			} catch (Exception e) {
				log.error("failed to update flue gas cleaning", e);
			}
		}

		private void delete(TableViewer table) {
			FlueGasCleaning c = Viewers.getFirstSelected(table);
			if (c == null || c.isProtected)
				return;
			boolean doIt = MsgBox.ask("Wirklich löschen?",
					"Soll die ausgewählte Rauchgasreinigung wirklich gelöscht werden?");
			if (!doIt)
				return;
			List<SearchResult> usage = new UsageSearch(App.getDb()).of(c);
			if (!usage.isEmpty()) {
				UsageError.show(usage);
				return;
			}
			try {
				dao.delete(c);
				cleanings.remove(c);
				table.setInput(cleanings);
			} catch (Exception e) {
				log.error("failed to delete flue gas cleaning " + c, e);
			}
		}
	}

	private class Label extends BaseTableLabel {

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof FlueGasCleaning))
				return null;
			FlueGasCleaning c = (FlueGasCleaning) obj;
			if (col < 3)
				return ProductTables.getText(c, col);
			switch (col) {
			case 3:
				return c.fuel == null ? null : (c.fuel.name);
			case 4:
				return Num.str(c.maxProducerPower) + " KW";
			case 5:
				return Num.str(c.maxVolumeFlow) + " m3/h";
			default:
				return null;
			}
		}
	}
}