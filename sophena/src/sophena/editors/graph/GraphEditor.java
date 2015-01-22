package sophena.editors.graph;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.CreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.GraphicalEditorWithPalette;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.actions.ActionFactory;

import sophena.model.FacilityType;
import sophena.model.Sample;
import sophena.rcp.Images;

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
		createZoom(viewer);
		createKeyBindings(viewer);
		viewer.setEditPartFactory(new PartFactory());
		ContextMenu menu = new ContextMenu(viewer, getActionRegistry());
		viewer.setContextMenu(menu);
	}

	private void createZoom(GraphicalViewer viewer) {
		ScalableRootEditPart root = (ScalableRootEditPart) viewer
				.getRootEditPart();
		ZoomManager man = root.getZoomManager();
		ZoomInAction in = new ZoomInAction(man);
		in.setAccelerator(SWT.KEYPAD_ADD);
		getActionRegistry().registerAction(in);
		ZoomOutAction out = new ZoomOutAction(man);
		out.setAccelerator(SWT.KEYPAD_SUBTRACT);
		getActionRegistry().registerAction(out);
		double[] levels = { 0.25, 0.5, 0.75, 1.0, 1.5, 2.0, 2.5, 3.0, 4.0, 5.0,
				10.0, 20.0 };
		man.setZoomLevels(levels);
		man.setZoomLevelContributions(Arrays.asList(ZoomManager.FIT_ALL,
				ZoomManager.FIT_HEIGHT, ZoomManager.FIT_WIDTH));
		viewer.setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.NONE),
				MouseWheelZoomHandler.SINGLETON);
	}

	private void createKeyBindings(GraphicalViewer viewer) {
		KeyHandler handler = new KeyHandler();
		ActionRegistry reg = getActionRegistry();
		handler.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
				reg.getAction(ActionFactory.DELETE.getId()));
		handler.put(KeyStroke.getPressed('+', SWT.KEYPAD_ADD, 0),
				reg.getAction(GEFActionConstants.ZOOM_IN));
		handler.put(KeyStroke.getPressed('-', SWT.KEYPAD_SUBTRACT, 0),
				reg.getAction(GEFActionConstants.ZOOM_OUT));
		viewer.setKeyHandler(handler);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class type) {
		if (Objects.equals(type, ZoomManager.class))
			return ((ScalableRootEditPart) getGraphicalViewer()
					.getRootEditPart()).getZoomManager();
		return super.getAdapter(type);
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
						FacilityType.PRODUCER), Images.PRODUCER_16.des(), null);
		group.add(producerEntry);
		CreationToolEntry consumerEntry = new CreationToolEntry("@Consumer",
				"@Creates a consumer", new FacilityFactory(
						FacilityType.CONSUMER), Images.CONSUMER_16.des(), null);

		group.add(consumerEntry);
		CreationToolEntry pumpEntry = new CreationToolEntry("@Pump",
				"@Creates a pump", new FacilityFactory(FacilityType.PUMP),
				Images.PUMP_16.des(), null);
		group.add(pumpEntry);

		CreationToolEntry pipeEntry = new ConnectionCreationToolEntry("@Pipe",
				"@Creates a new pipe between two facilities",
				new PipeCreationFactory(), null, null);
		group.add(pipeEntry);
		return root;
	}

}
