package sophena.rcp.editors.basedata.transfer.stations;

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
import sophena.model.ProductType;
import sophena.model.TransferStation;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.editors.Editor;
import sophena.rcp.editors.basedata.BaseTableLabel;
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

public class TransferStationEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("data.transfer.stations",
				"Hausübergabestationen");
		Editors.open(input, "sophena.TransferStationEditor");
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

		RootEntityDao<TransferStation> dao;
		List<TransferStation> stations;

		Page() {
			super(TransferStationEditor.this, "TransferStationPage",
					"Hausübergabestationen");
			dao = new RootEntityDao<>(TransferStation.class, App.getDb());
			stations = dao.getAll();
			Sorters.byName(stations);
		}

		@Override
		protected void createFormContent(IManagedForm managedForm) {
			ScrolledForm form = UI.formHeader(managedForm, "Hausübergabestationen");
			FormToolkit tk = managedForm.getToolkit();
			Composite body = UI.formBody(form, tk);
			createSection(body, tk);
			form.reflow(true);
		}

		private void createSection(Composite body, FormToolkit toolkit) {
			Section section = UI.section(body, toolkit, "Hausübergabestationen");
			UI.gridData(section, true, true);
			Composite comp = UI.sectionClient(section, toolkit);
			UI.gridLayout(comp, 1);
			TableViewer table = Tables.createViewer(comp, "Bezeichnung",
					"Gebäudetyp", "Link", "Preis", "Leistung");
			table.setLabelProvider(new Label());
			table.setInput(stations);
			double x = 1.0 / 5.0;
			Tables.bindColumnWidths(table, x, x, x, x, x);
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
			TransferStation s = new TransferStation();
			s.id = UUID.randomUUID().toString();
			s.type = ProductType.TRANSFER_STATION;
			s.name = "Neue Hausübergabestation";
			if (TransferStationWizard.open(s) != Window.OK)
				return;
			dao.insert(s);
			stations.add(s);
			table.setInput(stations);
		}

		private void edit(TableViewer table) {
			TransferStation s = Viewers.getFirstSelected(table);
			if (s == null)
				return;
			if (TransferStationWizard.open(s) != Window.OK)
				return;
			try {
				int idx = stations.indexOf(s);
				s = dao.update(s);
				stations.set(idx, s);
				table.setInput(stations);
			} catch (Exception e) {
				log.error("failed to update transfer station", e);
			}
		}

		private void delete(TableViewer table) {
			TransferStation s = Viewers.getFirstSelected(table);
			if (s == null || s.isProtected)
				return;
			boolean doIt = MsgBox.ask("Wirklich löschen?",
					"Soll die ausgewählte Hausübergabestation wirklich gelöscht werden?");
			if (!doIt)
				return;
			List<SearchResult> usage = new UsageSearch(App.getDb()).of(s);
			if (!usage.isEmpty()) {
				UsageError.show(usage);
				return;
			}
			try {
				dao.delete(s);
				stations.remove(s);
				table.setInput(stations);
			} catch (Exception e) {
				log.error("failed to delete transfer station " + s, e);
			}
		}

	}

	private class Label extends BaseTableLabel {

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof TransferStation))
				return null;
			TransferStation s = (TransferStation) obj;
			switch (col) {
			case 0:
				return s.name;
			case 1:
				return s.buildingType;
			case 2:
				return s.url;
			case 3:
				return s.purchasePrice == null ? null
						: Num.str(s.purchasePrice) + " EUR";
			case 4:
				return Num.str(s.outputCapacity) + " kW";
			default:
				return null;
			}
		}

	}

}
