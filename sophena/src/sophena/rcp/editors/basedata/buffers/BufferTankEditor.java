package sophena.rcp.editors.basedata.buffers;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import sophena.db.daos.RootEntityDao;
import sophena.model.BufferTank;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.Numbers;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;

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

		private RootEntityDao<BufferTank> dao;
		private List<BufferTank> buffers;

		Page() {
			super(BufferTankEditor.this, "BufferEditorPage", "Pufferspeicher");
			dao = new RootEntityDao<>(BufferTank.class, App.getDb());
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
			BufferTank b = new BufferTank();
			b.id = UUID.randomUUID().toString();
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

		private void delete(TableViewer table) {
			BufferTank b = Viewers.getFirstSelected(table);
			if (b == null)
				return;
			boolean doIt = MsgBox.ask("Wirklich löschen?",
					"Soll der ausgewählte Pufferspeicher wirklich gelöscht werden?");
			if (!doIt)
				return;
			try {
				dao.delete(b);
				buffers.remove(b);
				table.setInput(buffers);
			} catch (Exception e) {
				log.error("failed to delete buffer " + b, e);
			}
		}
	}

	private class Label extends LabelProvider implements ITableLabelProvider {
		@Override
		public Image getColumnImage(Object obj, int col) {
			return col == 0 ? Images.BUFFER_16.img() : null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof BufferTank))
				return null;
			BufferTank b = (BufferTank) obj;
			switch (col) {
			case 0:
				return b.name;
			case 1:
				return b.url;
			case 2:
				return b.purchasePrice == null ? null
						: Numbers.toString(b.purchasePrice) + " EUR";
			case 3:
				return Numbers.toString(b.volume) + " L";
			default:
				return null;
			}
		}
	}

}
