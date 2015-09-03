package sophena.rcp.editors.basedata;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.db.daos.RootEntityDao;
import sophena.model.BuildingState;
import sophena.rcp.App;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.UI;

public class BuildingStateEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("data.building.states",
				"Gebäudetypen");
		Editors.open(input, "sophena.BuildingStateEditor");
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

		private RootEntityDao<BuildingState> dao;
		private List<BuildingState> states;

		public Page() {
			super(BuildingStateEditor.this, "BuildingStatePage",
					"Gebäudetypen");
			dao = new RootEntityDao<>(BuildingState.class, App.getDb());
			states = dao.getAll();
		}

		@Override
		protected void createFormContent(IManagedForm managedForm) {
			ScrolledForm form = UI.formHeader(managedForm, "Gebäudetypen");
			FormToolkit toolkit = managedForm.getToolkit();
			Composite body = UI.formBody(form, toolkit);
			// TODO: create page content
			form.reflow(true);
		}
	}
}
