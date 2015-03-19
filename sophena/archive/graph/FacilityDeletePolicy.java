package sophena.rcp.editors.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import sophena.model.Consumer;
import sophena.model.Facility;
import sophena.model.Pipe;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.Pump;

public class FacilityDeletePolicy extends ComponentEditPolicy {

	@Override
	protected Command createDeleteCommand(GroupRequest req) {
		List<?> parts = req.getEditParts();
		if (parts == null || parts.isEmpty())
			return null;
		Object target = parts.get(0);
		if (!(target instanceof FacilityPart))
			return null;
		FacilityPart part = (FacilityPart) target;
		DeleteCommand command = new DeleteCommand(part);
		return command;
	}

	private class DeleteCommand extends Command {

		private Facility facility;
		private ProjectPart projectPart;
		private Project project;

		public DeleteCommand(FacilityPart part) {
			this.facility = (Facility) part.getModel();
			this.projectPart = (ProjectPart) part.getParent();
			this.project = (Project) projectPart.getModel();
		}

		@Override
		public boolean canExecute() {
			return facility != null && project != null
					&& project.getFacilities().contains(facility);
		}

		@Override
		public void execute() {
			removePipes(facility);
			List<?> list = getProjectList();
			if (list != null && !list.isEmpty())
				list.remove(facility);
			projectPart.refresh();
		}

		private void removePipes(Facility facility) {
			List<Pipe> removals = new ArrayList<>();
			String id = facility.getId();
			for (Pipe pipe : project.getPipes()) {
				if (Objects.equals(id, pipe.getProviderId())
						|| Objects.equals(id, pipe.getRecipientId()))
					removals.add(pipe);
			}
			project.getPipes().removeAll(removals);
		}

		private List<?> getProjectList() {
			if (facility instanceof Producer)
				return project.getProducers();
			if (facility instanceof Consumer)
				return project.getConsumers();
			if (facility instanceof Pump)
				return project.getPumps();
			else
				return Collections.emptyList();
		}

	}
}
