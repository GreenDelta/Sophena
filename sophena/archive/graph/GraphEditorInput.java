package sophena.rcp.editors.graph;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import sophena.model.ProjectDescriptor;

public class GraphEditorInput implements IEditorInput {

	private ProjectDescriptor project;

	public GraphEditorInput(ProjectDescriptor project) {
		this.project = project;
	}

	public ProjectDescriptor getProject() {
		return project;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return project.getName();
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return project.getName();
	}

}
