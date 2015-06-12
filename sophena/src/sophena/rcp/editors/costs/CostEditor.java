package sophena.rcp.editors.costs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ProjectDao;
import sophena.model.CostSettings;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.editors.CostSettingsPage;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class CostEditor extends Editor {

	private Logger log = LoggerFactory.getLogger(getClass());

	private String projectId;

	public static void open(ProjectDescriptor project) {
		if (project == null)
			return;
		EditorInput input = new EditorInput(project.getId() + "/net",
				project.getName());
		input.projectId = project.getId();
		Editors.open(input, "sophena.CostEditor");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		EditorInput i = (EditorInput) input;
		projectId = i.projectId;
	}

	@Override
	protected void addPages() {
		try {
			ProjectDao dao = new ProjectDao(App.getDb());
			Project project = dao.get(projectId);
			addPage(new OverviewPage(this));
			CostSettings settings = project.getCostSettings();
			if (settings != null) {
				addPage(new CostSettingsPage(this, settings));
			}
		} catch (Exception e) {
			log.error("failed to add editor pages", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	private static class EditorInput extends KeyEditorInput {

		private String projectId;

		public EditorInput(String key, String name) {
			super(key, name);
		}
	}
}
