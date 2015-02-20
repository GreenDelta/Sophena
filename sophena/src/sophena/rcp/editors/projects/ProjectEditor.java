package sophena.rcp.editors.projects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.forms.editor.FormEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sophena.model.Project;
import sophena.model.ProjectDescriptor;
import sophena.rcp.editors.graph.GraphEditor;
import sophena.rcp.editors.graph.GraphEditorInput;
import sophena.rcp.utils.Cache;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class ProjectEditor extends FormEditor {

	private Logger log = LoggerFactory.getLogger(getClass());

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
			ProjectDescriptor descriptor = new ProjectDescriptor();
			descriptor.setName("Test");
			GraphEditorInput input = new GraphEditorInput(descriptor);
			int graphIdx = addPage(new GraphEditor(), input);
			setPageText(graphIdx, "#Projektgraph");
		} catch (Exception e) {
			log.error("failed to add editor pages", e);
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
