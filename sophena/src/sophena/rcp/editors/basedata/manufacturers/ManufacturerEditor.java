package sophena.rcp.editors.basedata.manufacturers;

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

import sophena.db.daos.Dao;
import sophena.model.Manufacturer;
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

public class ManufacturerEditor extends Editor {

	private Logger log = LoggerFactory.getLogger(getClass());

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("data.manufacturers", M.Manufacturers);
		Editors.open(input, "sophena.ManufacturerEditor");
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

		private Dao<Manufacturer> dao = new Dao<>(Manufacturer.class, App.getDb());
		private List<Manufacturer> manufacturers = new ArrayList<>();

		public Page() {
			super(ManufacturerEditor.this, "ManufacturerEditorPage", M.Manufacturers);
			initData();
		}

		private void initData() {
			List<Manufacturer> all = dao.getAll();
			Sorters.sortBaseData(all);
			for (Manufacturer f : all) {
				manufacturers.add(f);
			}
		}

		@Override
		protected void createFormContent(IManagedForm managedForm) {
			ScrolledForm form = UI.formHeader(managedForm, M.Manufacturers);
			FormToolkit toolkit = managedForm.getToolkit();
			Composite body = UI.formBody(form, toolkit);
			Section section = UI.section(body, toolkit, M.Manufacturers);
			UI.gridData(section, true, true);
			Composite comp = UI.sectionClient(section, toolkit);
			UI.gridLayout(comp, 1);
			TableViewer table = Tables.createViewer(comp, "Name",
					"Adresse", "Weblink");
			table.setLabelProvider(new Label());
			table.setInput(manufacturers);
			double w = 1d / 3d;
			Tables.bindColumnWidths(table, w, w, w);
			bindActions(section, table);
			form.reflow(true);
		}

		private void bindActions(Section section, TableViewer table) {
			Action add = Actions.create(M.Add, Icon.ADD_16.des(),
					() -> addManufacturer(table));
			Action edit = Actions.create(M.Edit, Icon.EDIT_16.des(),
					() -> edit(table));
			Action del = Actions.create(M.Delete, Icon.DELETE_16.des(),
					() -> delete(table));
			Actions.bind(section, add, edit, del);
			Actions.bind(table, add, edit, del);
			Tables.onDoubleClick(table, (e) -> edit(table));
		}

		private void addManufacturer(TableViewer table) {
			Manufacturer m = new Manufacturer();
			m.id = UUID.randomUUID().toString();
			m.name = "Neuer Hersteller";
			m.url = "";
			if (ManufacturerWizard.open(m) != Window.OK)
				return;
			try {
				dao.insert(m);
				manufacturers.add(m);
				table.setInput(manufacturers);
			} catch (Exception e) {
				log.error("failed to add manufacturer");
			}

		}

		private void edit(TableViewer table) {
			Manufacturer m = Viewers.getFirstSelected(table);
			if (m == null)
				return;
			if (ManufacturerWizard.open(m) != Window.OK)
				return;
			try {
				int idx = manufacturers.indexOf(m);
				m = (Manufacturer) dao.update(m);
				manufacturers.set(idx, m);
				table.setInput(manufacturers);
			} catch (Exception e) {
				log.error("failed to update manufacturer");
			}
		}

		private void delete(TableViewer table) {
			Manufacturer m = Viewers.getFirstSelected(table);
			if (m == null || m.isProtected)
				return;
			boolean doIt = MsgBox.ask("Wirklich löschen?",
					"Soll der ausgewählte Hersteller wirklich gelöscht werden?");
			if (!doIt)
				return;
			try {
				dao.delete(m);
				manufacturers.remove(m);
				table.setInput(manufacturers);
			} catch (Exception e) {
				log.error("failed to delete Manufacturer " + m, e);
			}
		}

		private class Label extends BaseTableLabel {

			@Override
			public String getColumnText(Object element, int col) {
				if (!(element instanceof Manufacturer))
					return null;
				Manufacturer m = (Manufacturer) element;
				switch (col) {
				case 0:
					return m.name;
				case 1:
					return m.address;
				case 2:
					return m.url;
				default:
					return null;
				}
			}
		}
	}
}
