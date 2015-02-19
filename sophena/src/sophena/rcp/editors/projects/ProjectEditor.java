package sophena.rcp.editors.projects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.forms.editor.FormEditor;

import sophena.model.Project;
import sophena.rcp.utils.Cache;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class ProjectEditor extends FormEditor {

	public static void open(Project project) {
		if (project == null)
			return;
		String key = Cache.put(project);
		KeyEditorInput input = new KeyEditorInput(key, project.getName());
		Editors.open(input, "sophena.ProjectEditor");
	}

	@Override
	protected void addPages() {
		try {
			addPage(new InfoPage(this));
		} catch (Exception e) {
			e.printStackTrace(); // TODO: log
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

}
