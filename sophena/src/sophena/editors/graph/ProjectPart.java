package sophena.editors.graph;

import java.util.List;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ManhattanConnectionRouter;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.swt.SWT;

import sophena.model.Project;

public class ProjectPart extends AbstractGraphicalEditPart {

	@Override
	protected IFigure createFigure() {
		ProjectFigure figure = new ProjectFigure();
		ConnectionLayer layer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
		layer.setAntialias(SWT.ON);
		layer.setConnectionRouter(new ManhattanConnectionRouter());
		return figure;
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ProjectEditPolicy());
	}

	@Override
	protected void refreshVisuals() {
		Project project = (Project) getModel();
		ProjectFigure figure = (ProjectFigure) getFigure();
		figure.setLabel(project.getName());
		for (Object child : getChildren()) {
			if (child instanceof ProducerPart) {
				ProducerPart part = (ProducerPart) child;
				// part.refreshVisuals();
				part.refresh();
			}
		}
	}

	@Override
	protected List<?> getModelChildren() {
		Project project = (Project) getModel();
		return project.getProducers();
	}
}
