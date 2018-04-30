package sophena.rcp.editors.projects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import sophena.db.daos.ProjectDao;
import sophena.model.CostSettings;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.editors.CostSettingsPage;
import sophena.rcp.editors.Editor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.UI;

public class ProjectEditor extends Editor {

	private Project project;
	private CostSettingsPage settingsPage;

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
			CostSettings settings = project.costSettings;
			if (settings != null) {
				settingsPage = new CostSettingsPage(this, settings);
				settingsPage.setForProject(true);
				addPage(settingsPage);
			}
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
			dbProject.duration = project.duration;
			dbProject.weatherStation = project.weatherStation;
			if (settingsPage != null) {
				dbProject.costSettings = settingsPage.getCosts();
			}
			project = dao.update(dbProject);
			setPartName(project.name);
			Navigator.refresh();
			setSaved();
		} catch (Exception e) {
			log.error("failed to update project " + project, e);
		}
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	public void doSaveAs() {
		InputDialog dialog = new InputDialog(UI.shell(), "Speichern unter...",
				"Name des Projekts", project.name + " - Kopie", null);
		if (dialog.open() != Window.OK)
			return;
		ProjectDao dao = new ProjectDao(App.getDb());
		Project copy = project.clone();
		copy.name = dialog.getValue();
		dao.insert(copy);
		Navigator.refresh();
	}
}
