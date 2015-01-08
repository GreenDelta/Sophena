package sophena.editors.graph;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.ui.IEditorInput;

import sophena.model.Sample;

public class GraphEditor extends GraphicalEditor {

	public GraphEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}

	@Override
	protected void initializeGraphicalViewer() {
		IEditorInput editorInput = getEditorInput();
		if (!(editorInput instanceof GraphEditorInput))
			return;
		GraphEditorInput input = (GraphEditorInput) editorInput;
		setPartName(input.getName());
		GraphicalViewer viewer = getGraphicalViewer();
		viewer.setContents(Sample.get());
	}

	@Override
	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		GraphicalViewer viewer = getGraphicalViewer();
		viewer.setEditPartFactory(new PartFactory());
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

}
