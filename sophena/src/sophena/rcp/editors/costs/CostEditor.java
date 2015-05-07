package sophena.rcp.editors.costs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class CostEditor extends FormEditor {

	private Logger log = LoggerFactory.getLogger(getClass());

	public static void open(ProjectDescriptor project) {
		if(project == null)
			return;
		EditorInput input = new EditorInput(project.getId() + "/net",
				project.getName());
		input.projectId = project.getId();
		Editors.open(input, "sophena.CostEditor");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		EditorInput i = (EditorInput) input;
		// TODO: init editor data
	}

	@Override
	protected void addPages() {
		try {
		  	addPage(new OverviewPage(this));
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

	private static class EditorInput extends KeyEditorInput {

		private String projectId;

		public EditorInput(String key, String name) {
			super(key, name);
		}

	}

}
