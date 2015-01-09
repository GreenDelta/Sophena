package sophena.editors.graph;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;

import sophena.model.Consumer;
import sophena.model.Facility;
import sophena.model.Producer;
import sophena.model.Project;

public class FacilityCreationCommand extends Command {

	private EditPart parent;
	private Project project;
	private Facility facility;
	private int x;
	private int y;

	public FacilityCreationCommand(EditPart parent) {
		this.parent = parent;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public boolean canExecute() {
		return project != null && facility != null;
	}

	@Override
	public void execute() {
		facility.setX(x);
		facility.setY(y);
		if (facility instanceof Producer)
			project.getProducers().add((Producer) facility);
		else if (facility instanceof Consumer)
			project.getConsumers().add((Consumer) facility);
		parent.refresh();
	}

	@Override
	public boolean canUndo() {
		if (project == null || facility == null)
			return false;
		return project.getProducers().contains(facility);
	}

	@Override
	public void undo() {
		project.getProducers().remove(facility);
	}

}
