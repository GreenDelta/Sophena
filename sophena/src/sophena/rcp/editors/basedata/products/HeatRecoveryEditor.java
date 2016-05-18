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
import sophena.model.HeatRecovery;
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

public class HeatRecoveryEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("heat.recovery.products",
				"Wärmerückgewinnung");
		Editors.open(input, "sophena.products.HeatRecoveryEditor");
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

		RootEntityDao<HeatRecovery> dao;
		List<HeatRecovery> recoveries;

		Page() {
			super(HeatRecoveryEditor.this, "HeatRecoveryPage",
					"Wärmerückgewinnungsanlagen");
			dao = new RootEntityDao<>(HeatRecovery.class, App.getDb());
			recoveries = dao.getAll();
			Sorters.byName(recoveries);
		}

		@Override
		protected void createFormContent(IManagedForm managedForm) {
			ScrolledForm form = UI.formHeader(managedForm, "Wärmerückgewinnungsanlagen");
			FormToolkit tk = managedForm.getToolkit();
			Composite body = UI.formBody(form, tk);
			createSection(body, tk);
			form.reflow(true);
		}

		private void createSection(Composite body, FormToolkit toolkit) {
			Section section = UI.section(body, toolkit, "Wärmerückgewinnungsanlagen");
			UI.gridData(section, true, true);
			Composite comp = UI.sectionClient(section, toolkit);
			UI.gridLayout(comp, 1);
			TableViewer table = Tables.createViewer(comp, "Produktgruppe", "Bezeichnung", "Hersteller", "Leistung",
					"Brennstoff (Erz.)", "Leistung (Erz.)");
			table.setLabelProvider(new Label());
			table.setInput(recoveries);
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
			HeatRecovery r = new HeatRecovery();
			r.id = UUID.randomUUID().toString();
			r.type = ProductType.HEAT_RECOVERY;
			r.name = "Neue Wärmerückgewinnung";
			if (HeatRecoveryWizard.open(r) != Window.OK)
				return;
			dao.insert(r);
			recoveries.add(r);
			table.setInput(recoveries);
		}

		private void edit(TableViewer table) {
			HeatRecovery r = Viewers.getFirstSelected(table);
			if (r == null)
				return;
			if (HeatRecoveryWizard.open(r) != Window.OK)
				return;
			try {
				int idx = recoveries.indexOf(r);
				r = dao.update(r);
				recoveries.set(idx, r);
				table.setInput(recoveries);
			} catch (Exception e) {
				log.error("failed to update", e);
			}
		}

		private void delete(TableViewer table) {
			HeatRecovery r = Viewers.getFirstSelected(table);
			if (r == null)
				return;
			boolean doIt = MsgBox.ask("Wirklich löschen?",
					"Soll die ausgewählte Wärmerückgewinnung wirklich gelöscht werden?");
			if (!doIt)
				return;
			List<SearchResult> usage = new UsageSearch(App.getDb()).of(r);
			if (!usage.isEmpty()) {
				UsageError.show(usage);
				return;
			}
			try {
				dao.delete(r);
				recoveries.remove(r);
				table.setInput(recoveries);
			} catch (Exception e) {
				log.error("failed to delete " + r, e);
			}
		}
	}

	private class Label extends BaseTableLabel {

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof HeatRecovery))
				return null;
			HeatRecovery hrc = (HeatRecovery) obj;
			if (col < 3)
				return ProductTables.getText(hrc, col);
			switch (col) {
			case 3:
				return Num.str(hrc.power) + " kW";
			case 4:
				return hrc.fuel;
			case 5:
				return Num.str(hrc.producerPower) + " kW";
			default:
				return null;
			}
		}
	}
}
