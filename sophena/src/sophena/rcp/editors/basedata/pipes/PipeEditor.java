package sophena.rcp.editors.basedata.pipes;

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
import sophena.model.Pipe;
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

public class PipeEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("data.pipes",
				"Wärmeleitungen");
		Editors.open(input, "sophena.PipeEditor");
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

		private RootEntityDao<Pipe> dao;
		private List<Pipe> pipes;

		public Page() {
			super(PipeEditor.this, "PipeEditorPage", "Wärmeleitungen");
			dao = new RootEntityDao<>(Pipe.class, App.getDb());
			pipes = dao.getAll();
			Sorters.pipes(pipes);
		}

		@Override
		protected void createFormContent(IManagedForm managedForm) {
			ScrolledForm form = UI.formHeader(managedForm, "Wärmeleitungen");
			FormToolkit toolkit = managedForm.getToolkit();
			Composite body = UI.formBody(form, toolkit);
			createSection(body, toolkit);
			form.reflow(true);
		}

		private void createSection(Composite body, FormToolkit toolkit) {
			Section section = UI.section(body, toolkit, "Wärmeleitungen");
			UI.gridData(section, true, true);
			Composite comp = UI.sectionClient(section, toolkit);
			UI.gridLayout(comp, 1);
			TableViewer table = Tables.createViewer(comp, "Produktgruppe", "Bezeichnung", 
					"Hersteller", "Art", "Außend. Medienrohr", "U-Wert");
			table.setLabelProvider(new Label());
			table.setInput(pipes);
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
			Actions.bind(section, add, edit, del);
			Actions.bind(table, add, edit, del);
			Tables.onDoubleClick(table, (e) -> edit(table));
		}

		private void add(TableViewer table) {
			Pipe p = new Pipe();
			p.type = ProductType.PIPE;
			p.id = UUID.randomUUID().toString();
			p.name = "Neue Wärmeleitung";
			if (PipeWizard.open(p) != Window.OK)
				return;
			dao.insert(p);
			pipes.add(p);
			table.setInput(pipes);
		}

		private void edit(TableViewer table) {
			Pipe p = Viewers.getFirstSelected(table);
			if (p == null)
				return;
			if (PipeWizard.open(p) != Window.OK)
				return;
			try {
				int idx = pipes.indexOf(p);
				p = dao.update(p);
				pipes.set(idx, p);
				table.setInput(pipes);
			} catch (Exception e) {
				log.error("failed to update pipe");
			}
		}

		private void saveAs(TableViewer table) {
			Pipe p = Viewers.getFirstSelected(table);
			if (p == null)
				return;
			Pipe copy = p.clone();
			copy.id = UUID.randomUUID().toString();
			copy.isProtected = false;
			if (PipeWizard.open(copy) != Window.OK)
				return;
			dao.insert(copy);
			pipes.add(copy);
			table.setInput(pipes);
		}

		private void delete(TableViewer table) {
			Pipe p = Viewers.getFirstSelected(table);
			if (p == null || p.isProtected)
				return;
			boolean doIt = MsgBox.ask("Wirklich löschen?",
					"Soll die ausgewählte Wärmeleitung wirklich gelöscht werden?");
			if (!doIt)
				return;
			List<SearchResult> usage = new UsageSearch(App.getDb()).of(p);
			if (!usage.isEmpty()) {
				UsageError.show(usage);
				return;
			}
			try {
				dao.delete(p);
				pipes.remove(p);
				table.setInput(pipes);
			} catch (Exception e) {
				log.error("failed to delete pipe " + p, e);
			}
		}

	}

	private class Label extends BaseTableLabel {

		@Override
		public String getColumnText(Object e, int col) {
			if (!(e instanceof Pipe))
				return null;
			Pipe p = (Pipe) e;
			if (col < 3)
				return ProductTables.getText(p, col);
			switch (col) {
			case 3:
				return p.pipeType != null ? p.pipeType.name() : null;
			case 4:
				return Num.str(p.outerDiameter) + " mm";
			case 5:
				return Num.str(p.uValue) + "W/m*K";
			default:
				return null;
			}
		}
	}
}
