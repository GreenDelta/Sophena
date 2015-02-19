package sophena.rcp.editors.graph;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;

import sophena.model.Facility;

public class FacilityLayoutCommand extends Command {

	private EditPart parent;
	private Facility facility;
	private int x;
	private int y;

	public FacilityLayoutCommand(EditPart parent) {
		this.parent = parent;
	}

	@Override
	public boolean canExecute() {
		return facility != null;
	}

	@Override
	public void execute() {
		facility.setX(x);
		facility.setY(y);
		parent.refresh();
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

}
