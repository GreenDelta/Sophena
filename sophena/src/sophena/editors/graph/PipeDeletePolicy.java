package sophena.editors.graph;

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import sophena.model.Pipe;
import sophena.model.Project;

public class PipeDeletePolicy extends ConnectionEditPolicy {

	@Override
	protected Command getDeleteCommand(GroupRequest req) {
		List<?> parts = req.getEditParts();
		if (parts == null || parts.isEmpty())
			return null;
		Object target = parts.get(0);
		if (!(target instanceof PipePart))
			return null;
		PipePart part = (PipePart) target;
		return new DeleteCommand(part);
	}

	private class DeleteCommand extends Command {

		private Pipe pipe;
		private ProjectPart projectPart;
		private Project project;

		public DeleteCommand(PipePart part) {
			this.pipe = (Pipe) part.getModel();
			ScalableRootEditPart parent = (ScalableRootEditPart) part
					.getParent();
			this.projectPart = (ProjectPart) parent.getContents();
			this.project = (Project) projectPart.getModel();
		}

		@Override
		public boolean canExecute() {
			return pipe != null && project != null
					&& project.getPipes().contains(pipe);
		}

		@Override
		public void execute() {
			project.getPipes().remove(pipe);
			projectPart.refresh();
		}
	}

}
