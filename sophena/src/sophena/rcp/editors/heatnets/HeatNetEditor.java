package sophena.rcp.editors.heatnets;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ProjectDao;
import sophena.model.HeatNet;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class HeatNetEditor extends Editor {

	private Logger log = LoggerFactory.getLogger(getClass());

	protected Project project;
	protected HeatNet heatNet;

	public static void open(ProjectDescriptor project) {
		if (project == null)
			return;
		EditorInput input = new EditorInput(project.id + "/net",
				project.name);
		input.projectId = project.id;
		Editors.open(input, "sophena.HeatNetEditor");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		EditorInput i = (EditorInput) input;
		ProjectDao dao = new ProjectDao(App.getDb());
		project = dao.get(i.projectId);
		heatNet = project.heatNet;
		setPartName(project.name);
	}

	@Override
	protected void addPages() {
		try {
			addPage(new HeatNetPage(this));
		} catch (Exception e) {
			log.error("failed to add editor pages", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			log.info("update heat net in project {}", project);
			ProjectDao dao = new ProjectDao(App.getDb());
			Project p = dao.get(project.id);
			p.heatNet = heatNet;
			project = dao.update(p);
			heatNet = project.heatNet;
			setSaved();
		} catch (Exception e) {
			log.error("failed to update heat net in project " + project, e);
		}
	}

	private static class EditorInput extends KeyEditorInput {

		private String projectId;

		public EditorInput(String key, String name) {
			super(key, name);
		}

	}
}
