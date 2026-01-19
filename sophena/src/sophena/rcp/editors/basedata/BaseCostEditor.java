package sophena.rcp.editors.basedata;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
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
		var input = new KeyEditorInput("data.costsettings", "Basiseinstellungen");
		Editors.open(input, "sophena.BaseCostEditor");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
		throws PartInitException {
		super.init(site, input);
		try {
			var dao = new CostSettingsDao(App.getDb());
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
			var dao = new CostSettingsDao(App.getDb());
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
			super(editor, "CostSettingsPage", "Basiseinstellungen");
			this.editor = editor;
		}

		@Override
		protected void createFormContent(IManagedForm mform) {
			var form = UI.formHeader(mform, "Basiseinstellungen");
			toolkit = mform.getToolkit();
			var body = UI.formBody(form, toolkit);
			var panel = new CostSettingsPanel(editor, () -> costs, () -> {});
			panel.isForProject = false;
			panel.render(toolkit, body);
			form.reflow(true);
		}
	}
}
