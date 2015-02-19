package sophena.rcp.editors.graph;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;

import sophena.model.Facility;
import sophena.model.Project;

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
