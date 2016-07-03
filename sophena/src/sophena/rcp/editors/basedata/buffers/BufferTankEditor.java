package sophena.rcp.editors.basedata.buffers;

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
import sophena.model.BufferTank;
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

public class BufferTankEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("data.buffers",
				"Pufferspeicher");
		Editors.open(input, "sophena.BufferEditor");
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

		RootEntityDao<BufferTank> dao;
		List<BufferTank> buffers;

		Page() {
			super(BufferTankEditor.this, "BufferEditorPage", "Pufferspeicher");
			dao = new RootEntityDao<>(BufferTank.class, App.getDb());
			buffers = dao.getAll();
			Sorters.buffers(buffers);
		}

		@Override
		protected void createFormContent(IManagedForm managedForm) {
			ScrolledForm form = UI.formHeader(managedForm, "Pufferspeicher");
			FormToolkit toolkit = managedForm.getToolkit();
			Composite body = UI.formBody(form, toolkit);
			createSection(body, toolkit);
			form.reflow(true);
		}

		private void createSection(Composite body, FormToolkit toolkit) {
			Section section = UI.section(body, toolkit, "Pufferspeicher");
			UI.gridData(section, true, true);
			Composite comp = UI.sectionClient(section, toolkit);
			UI.gridLayout(comp, 1);
			TableViewer table = Tables.createViewer(comp, "Produktgruppe", "Bezeichnung", "Hersteller", "Volumen",
					"Durchmesser", "Höhe");
			table.setLabelProvider(new Label());
			table.setInput(buffers);
			double x = 1.0 / 6.0;
			Tables.bindColumnWidths(table, x, x, x, x, x, x);
			bindActions(section, table);
		}

		private void bindActions(Section section, TableViewer table) {
			Action add = Actions.create(M.Add, Icon.ADD_16.des(),
					() -> add(table));
			Action edit = Actions.create(M.Edit, Icon.EDIT_16.des(),
					() -> edit(table));
			Action saveAs = Actions.create(M.SaveAs, Icon.SAVE_AS_16.des(),
					() -> saveAs(table));
			Action del = Actions.create(M.Delete, Icon.DELETE_16.des(),
					() -> delete(table));
			Actions.bind(section, add, edit, saveAs, del);
			Actions.bind(table, add, edit, saveAs, del);
			Tables.onDoubleClick(table, (e) -> edit(table));
		}

		private void add(TableViewer table) {
			BufferTank b = new BufferTank();
			b.id = UUID.randomUUID().toString();
			b.type = ProductType.BUFFER_TANK;
			b.name = "Neuer Pufferspeicher";
			if (BufferTankWizard.open(b) != Window.OK)
				return;
			dao.insert(b);
			buffers.add(b);
			table.setInput(buffers);
		}

		private void edit(TableViewer table) {
			BufferTank b = Viewers.getFirstSelected(table);
			if (b == null)
				return;
			if (BufferTankWizard.open(b) != Window.OK)
				return;
			try {
				int idx = buffers.indexOf(b);
				b = dao.update(b);
				buffers.set(idx, b);
				table.setInput(buffers);
			} catch (Exception e) {
				log.error("failed to update buffer");
			}
		}

		private void saveAs(TableViewer table) {
			BufferTank b = Viewers.getFirstSelected(table);
			if (b == null)
				return;
			BufferTank copy = b.clone();
			copy.id = UUID.randomUUID().toString();
			copy.isProtected = false;
			if (BufferTankWizard.open(copy) != Window.OK)
				return;
			dao.insert(copy);
			buffers.add(copy);
			table.setInput(buffers);
		}

		private void delete(TableViewer table) {
			BufferTank b = Viewers.getFirstSelected(table);
			if (b == null || b.isProtected)
				return;
			boolean doIt = MsgBox.ask("Wirklich löschen?",
					"Soll der ausgewählte Pufferspeicher wirklich gelöscht werden?");
			if (!doIt)
				return;
			List<SearchResult> usage = new UsageSearch(App.getDb()).of(b);
			if (!usage.isEmpty()) {
				UsageError.show(usage);
				return;
			}
			try {
				dao.delete(b);
				buffers.remove(b);
				table.setInput(buffers);
			} catch (Exception e) {
				log.error("failed to delete buffer " + b, e);
			}
		}
	}

	private class Label extends BaseTableLabel {

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof BufferTank))
				return null;
			BufferTank b = (BufferTank) obj;
			if (col < 3)
				return ProductTables.getText(b, col);
			switch (col) {
			case 3:
				return Num.str(b.volume) + " L";
			case 4:
				return Num.str(b.diameter) != null ? Num.str(b.diameter) + " mm" : null;
			case 5:
				return Num.str(b.height) != null ? Num.str(b.height) + " mm" : null;
			default:
				return null;
			}
		}
	}

}
