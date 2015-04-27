package sophena.rcp.editors.projects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ProjectDao;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class ProjectEditor extends FormEditor {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Project project;
	private boolean dirty;

	public static void open(ProjectDescriptor d) {
		if (d == null)
			return;
		KeyEditorInput input = new KeyEditorInput(d.getId(), d.getName());
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
		setPartName(project.getName());
	}

	public void setDirty() {
		if (dirty)
			return;
		dirty = true;
		editorDirtyStateChanged();
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	protected void addPages() {
		try {
			addPage(new InfoPage(this));
			addPage(new ResultPage(this));
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
			project = dao.update(project);
			dirty = false;
			setPartName(project.getName());
			Navigator.refresh();
			editorDirtyStateChanged();
		} catch (Exception e) {
			log.error("failed to update project " + project, e);
		}
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

}
