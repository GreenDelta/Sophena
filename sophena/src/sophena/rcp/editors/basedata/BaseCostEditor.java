package sophena.rcp.editors.basedata;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.db.daos.CostSettingsDao;
import sophena.model.CostSettings;
import sophena.rcp.App;
import sophena.rcp.editors.CostSettingsPanel;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.UI;

public class BaseCostEditor extends Editor {

	private CostSettings costs;

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("data.costsettings",
				"Kosteneinstellungen");
		Editors.open(input, "sophena.BaseCostEditor");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		try {
			CostSettingsDao dao = new CostSettingsDao(App.getDb());
			costs = dao.getGlobal();
			if (costs == null) {
				log.warn("did not found global cost settings -> create new");
				costs = new CostSettings();
				costs.id = CostSettings.GLOBAL_ID;
				costs = dao.insert(costs);
			}
		} catch (Exception e) {
			log.error("failed to load global cost settings");
		}
	}

	@Override
	protected void addPages() {
		try {
			addPage(new Page(this));
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			CostSettingsDao dao = new CostSettingsDao(App.getDb());
			dao.update(costs);
			setSaved();
		} catch (Exception e) {
			log.error("failed to save global cost settings");
		}
	}

	private class Page extends FormPage {

		private Editor editor;
		private FormToolkit toolkit;

		Page(Editor editor) {
			super(editor, "CostSettingsPage", "Kosteneinstellungen");
			this.editor = editor;
		}

		@Override
		protected void createFormContent(IManagedForm mform) {
			String title = "Kosteneinstellungen";
			ScrolledForm form = UI.formHeader(mform, title);
			toolkit = mform.getToolkit();
			Composite body = UI.formBody(form, toolkit);
			CostSettingsPanel panel = new CostSettingsPanel(editor,
					() -> costs, () -> {});
			panel.isForProject = false;
			panel.render(toolkit, body);
			form.reflow(true);
		}

	}
}
