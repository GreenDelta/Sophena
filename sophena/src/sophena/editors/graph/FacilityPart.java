package sophena.editors.graph;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import sophena.editors.graph.figures.ConsumerFigure;
import sophena.editors.graph.figures.FacilityFigure;
import sophena.editors.graph.figures.ProducerFigure;
import sophena.editors.graph.figures.PumpFigure;
import sophena.model.Facility;
import sophena.model.FacilityType;
import sophena.model.Pipe;
import sophena.model.Project;

public class FacilityPart extends AbstractGraphicalEditPart implements
		NodeEditPart {

	private final FacilityType type;
	private ConnectionAnchor anchor;

	public FacilityPart(FacilityType type) {
		this.type = type;
	}

	@Override
	protected IFigure createFigure() {
		switch (type) {
		case CONSUMER:
			return new ConsumerFigure();
		case PRODUCER:
			return new ProducerFigure();
		case PUMP:
			return new PumpFigure();
		default:
			return new PumpFigure(); // TODO: distributor
		}
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE,
				new PipeConnectionPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new FacilityDeletePolicy());
	}

	@Override
	protected void refreshVisuals() {
		FacilityFigure figure = (FacilityFigure) getFigure();
		Facility facility = (Facility) getModel();
		figure.setLabel(facility.getName());
		figure.getParent().setConstraint(figure,
				new Rectangle(facility.getX(), facility.getY(), 50, 50));
	}

	@Override
	protected List<?> getModelSourceConnections() {
		return getConnectedPipes(true);
	}

	@Override
	protected List<?> getModelTargetConnections() {
		return getConnectedPipes(false);
	}

	private List<Pipe> getConnectedPipes(boolean asProvider) {
		Project project = (Project) getParent().getModel();
		List<Pipe> pipes = new ArrayList<>();
		Facility facility = (Facility) getModel();
		String id = facility.getId();
		if (id == null)
			return pipes;
		for (Pipe pipe : project.getPipes()) {
			if (asProvider && id.equals(pipe.getProviderId()))
				pipes.add(pipe);
			else if (!asProvider && id.equals(pipe.getRecipientId()))
				pipes.add(pipe);
		}
		return pipes;
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart con) {
		return getConnectionAnchor();
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request req) {
		return getConnectionAnchor();
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart con) {
		return getConnectionAnchor();
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request req) {
		return getConnectionAnchor();
	}

	private ConnectionAnchor getConnectionAnchor() {
		if (anchor == null)
			anchor = new ChopboxAnchor(getFigure());
		return anchor;
	}

}
