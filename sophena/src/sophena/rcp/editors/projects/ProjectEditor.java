package sophena.rcp.editors.projects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import sophena.db.daos.ProjectDao;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.editors.Editor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class ProjectEditor extends Editor {

	private Project project;

	public static void open(ProjectDescriptor d) {
		if (d == null)
			return;
		KeyEditorInput input = new KeyEditorInput(d.id, d.name);
		Editors.open(input, "sophena.ProjectEditor");
	}

	public Project getProject() {
		return project;
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		KeyEditorInput ki = (KeyEditorInput) input;
		ProjectDao dao = new ProjectDao(App.getDb());
		project = dao.get(ki.getKey());
		setPartName(project.name);
	}

	@Override
	protected void addPages() {
		try {
			addPage(new InfoPage(this));
			// ProjectDescriptor descriptor = new ProjectDescriptor();
			// descriptor.setName("Test");
			// GraphEditorInput input = new GraphEditorInput(descriptor);
			// int graphIdx = addPage(new GraphEditor(), input);
			// setPageText(graphIdx, "#Projektgraph");
		} catch (Exception e) {
			log.error("failed to add editor pages", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			log.info("update project {}", project);
			ProjectDao dao = new ProjectDao(App.getDb());
			Project dbProject = dao.get(project.id);
			dbProject.description = project.description;
			dbProject.name = project.name;
			dbProject.setProjectDuration(project.getProjectDuration());
			dbProject.setWeatherStation(project.getWeatherStation());
			project = dao.update(dbProject);
			setPartName(project.name);
			Navigator.refresh();
			setSaved();
		} catch (Exception e) {
			log.error("failed to update project " + project, e);
		}
	}
}
