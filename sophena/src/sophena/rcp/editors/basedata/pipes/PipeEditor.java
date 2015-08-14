package sophena.rcp.editors.basedata.pipes;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import sophena.db.daos.RootEntityDao;
import sophena.model.Pipe;
import sophena.rcp.App;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;

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
			Collections.sort(pipes,
					(p1, p2) -> Strings.compare(p1.name, p2.name));
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
			TableViewer table = Tables.createViewer(comp, "Bezeichnung",
					"Link", "Preis", "Durchmesser", "U-Wert");
			// table.setLabelProvider(new Label());
			table.setInput(pipes);
			Tables.bindColumnWidths(table, 0.2, 0.2, 0.2, 0.2, 0.2);
			// bindActions(section, table);
		}

	}

}