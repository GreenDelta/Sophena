package sophena.editors.graph;

import java.util.List;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ManhattanConnectionRouter;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
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
		installEditPolicy(EditPolicy.COMPONENT_ROLE,
				new RootComponentEditPolicy());
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new FacilityEditPolicy());
	}

	@Override
	protected void refreshVisuals() {
		Project project = (Project) getModel();
		ProjectFigure figure = (ProjectFigure) getFigure();
		figure.setLabel(project.getName());
		for (Object child : getChildren()) {
			if (child instanceof FacilityPart) {
				FacilityPart part = (FacilityPart) child;
				part.refresh();
			}
		}
	}

	@Override
	protected List<?> getModelChildren() {
		Project project = (Project) getModel();
		return project.getFacilities();
	}
}
