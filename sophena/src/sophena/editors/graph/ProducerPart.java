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

import sophena.model.Facility;
import sophena.model.Pipe;
import sophena.model.Producer;
import sophena.model.Project;

public class ProducerPart extends AbstractGraphicalEditPart implements
		NodeEditPart {

	@Override
	protected IFigure createFigure() {
		return new ProducerFigure();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE,
				new PipeConnectionPolicy());
	}

	@Override
	protected void refreshVisuals() {
		ProducerFigure figure = (ProducerFigure) getFigure();
		Producer producer = (Producer) getModel();
		figure.setLabel(producer.getName());
		figure.getParent().setConstraint(figure,
				new Rectangle(producer.getX(), producer.getY(), 100, 100));
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
			else if (id.equals(pipe.getRecipientId()))
				pipes.add(pipe);
		}
		return pipes;
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart con) {
		return new ChopboxAnchor(getFigure());
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request req) {
		return new ChopboxAnchor(getFigure());
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart con) {
		return new ChopboxAnchor(getFigure());
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request req) {
		return new ChopboxAnchor(getFigure());
	}

}
