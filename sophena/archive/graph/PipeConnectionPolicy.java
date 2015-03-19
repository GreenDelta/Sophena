package sophena.rcp.editors.graph;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

import sophena.model.Facility;

public class PipeConnectionPolicy extends GraphicalNodeEditPolicy {

	@Override
	protected Command getConnectionCompleteCommand(CreateConnectionRequest req) {
		PipeCreationCommand cmd = (PipeCreationCommand) req.getStartCommand();
		Facility recipient = (Facility) getHost().getModel();
		cmd.setRecipient(recipient);
		return cmd;
	}

	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest req) {
		ProjectPart projectPart = (ProjectPart) getHost().getParent();
		PipeCreationCommand cmd = new PipeCreationCommand(projectPart);
		Facility provider = (Facility) getHost().getModel();
		cmd.setProvider(provider);
		req.setStartCommand(cmd);
		return cmd;
	}

	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest req) {
		return null;
	}

	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest req) {
		return null;
	}
}
