package sophena.editors.graph;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.CreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.ui.parts.GraphicalEditorWithPalette;
import org.eclipse.ui.IEditorInput;

import sophena.model.FacilityType;
import sophena.model.Sample;

public class GraphEditor extends GraphicalEditorWithPalette {

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

	@Override
	protected PaletteRoot getPaletteRoot() {
		PaletteRoot root = new PaletteRoot();
		PaletteGroup group = new PaletteGroup("@Editing");
		root.add(group);
		SelectionToolEntry selectionEntry = new SelectionToolEntry();
		group.add(selectionEntry);
		root.setDefaultEntry(selectionEntry);
		group.add(new MarqueeToolEntry());

		CreationToolEntry producerEntry = new CreationToolEntry("@Producer",
				"@Creates a producer", new FacilityFactory(
						FacilityType.PRODUCER), null, null);
		group.add(producerEntry);
		CreationToolEntry consumerEntry = new CreationToolEntry("@Consumer",
				"@Creates a consumer", new FacilityFactory(
						FacilityType.CONSUMER), null, null);
		group.add(consumerEntry);
		CreationToolEntry pumpEntry = new CreationToolEntry("@Pump",
				"@Creates a pump", new FacilityFactory(FacilityType.PUMP),
				null, null);
		group.add(pumpEntry);

		CreationToolEntry pipeEntry = new ConnectionCreationToolEntry("@Pipe",
				"@Creates a new pipe between two facilities",
				new PipeCreationFactory(), null, null);
		group.add(pipeEntry);
		return root;
	}

}
