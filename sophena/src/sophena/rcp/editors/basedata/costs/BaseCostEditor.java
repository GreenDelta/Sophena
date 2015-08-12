package sophena.rcp.editors.basedata.costs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import sophena.db.daos.CostSettingsDao;
import sophena.model.CostSettings;
import sophena.rcp.App;
import sophena.rcp.editors.CostSettingsPage;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

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
			addPage(new CostSettingsPage(this, costs));
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
}
