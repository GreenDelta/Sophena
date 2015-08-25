package sophena.rcp.editors.basedata.buffers;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import sophena.db.daos.RootEntityDao;
import sophena.model.Buffer;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;

public class BufferEditor extends Editor {

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

		private RootEntityDao<Buffer> dao;
		private List<Buffer> buffers;

		Page() {
			super(BufferEditor.this, "BufferEditorPage", "Pufferspeicher");
			dao = new RootEntityDao<>(Buffer.class, App.getDb());
			buffers = dao.getAll();
			Collections.sort(buffers,
					(b1, b2) -> Strings.compare(b1.name, b2.name));
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
			TableViewer table = Tables.createViewer(comp, "Bezeichnung",
					"Link", "Preis", "Volumen");
			table.setLabelProvider(new Label());
			table.setInput(buffers);
			Tables.bindColumnWidths(table, 0.3, 0.3, 0.2, 0.2);
			bindActions(section, table);
		}

		private void bindActions(Section section, TableViewer table) {
			Action add = Actions.create(M.Add, Images.ADD_16.des(),
					() -> add(table));
			Action edit = Actions.create(M.Edit, Images.EDIT_16.des(),
					() -> edit(table));
			Action del = Actions.create(M.Delete, Images.DELETE_16.des(),
					() -> delete(table));
			Actions.bind(section, add, edit, del);
			Actions.bind(table, add, edit, del);
			Tables.onDoubleClick(table, (e) -> edit(table));
		}

		private void add(TableViewer table) {

		}

		private void edit(TableViewer table) {

		}

		private void delete(TableViewer table) {

		}
	}

	private class Label extends LabelProvider implements ITableLabelProvider {
		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			return null;
		}
	}

}
