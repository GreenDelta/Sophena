package sophena.rcp.editors.graph;

import java.util.List;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ManhattanConnectionRouter;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.swt.SWT;

import sophena.model.Facility;
import sophena.model.Project;
import sophena.rcp.editors.graph.figures.ProjectFigure;

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

	class FacilityEditPolicy extends XYLayoutEditPolicy {

		@Override
		protected Command createChangeConstraintCommand(
				ChangeBoundsRequest request, EditPart child, Object constraint) {
			if (!(child instanceof FacilityPart))
				return null;
			if (!(constraint instanceof Rectangle))
				return null;
			FacilityLayoutCommand command = new FacilityLayoutCommand(getHost());
			Rectangle rect = (Rectangle) constraint;
			command.setFacility((Facility) child.getModel());
			command.setX(rect.x);
			command.setY(rect.y);
			return command;
		}

		@Override
		protected Command getCreateCommand(CreateRequest request) {
			EditPart host = getHost();
			if (!(host instanceof ProjectPart))
				return null;
			if (request.getType() != REQ_CREATE)
				return null;
			Object obj = request.getNewObject();
			if (!(obj instanceof Facility))
				return null;
			Facility facility = (Facility) obj;
			Project project = (Project) host.getModel();
			FacilityCreationCommand command = new FacilityCreationCommand(host);
			command.setX(request.getLocation().x);
			command.setY(request.getLocation().y);
			command.setFacility(facility);
			command.setProject(project);
			return command;
		}
	}

}
