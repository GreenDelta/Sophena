package sophena.editors.graph;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;

import sophena.model.Producer;
import sophena.model.Project;

public class ProjectEditPolicy extends XYLayoutEditPolicy {

	@Override
	protected Command createChangeConstraintCommand(
			ChangeBoundsRequest request, EditPart child, Object constraint) {
		if (!(child instanceof ProducerPart))
			return null;
		if (!(constraint instanceof Rectangle))
			return null;
		ProducerLayoutCommand command = new ProducerLayoutCommand(getHost());
		Rectangle rect = (Rectangle) constraint;
		command.setProducer((Producer) child.getModel());
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
		Object type = request.getNewObjectType();
		if (!Producer.class.equals(type)) // TODO consumers
			return null;
		Producer producer = (Producer) request.getNewObject();
		Project project = (Project) host.getModel();
		ProducerCreationCommand command = new ProducerCreationCommand(host);
		command.setX(request.getLocation().x);
		command.setY(request.getLocation().y);
		command.setProducer(producer);
		command.setProject(project);
		return command;
	}

}
