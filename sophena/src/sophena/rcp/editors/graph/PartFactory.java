package sophena.rcp.editors.graph;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import sophena.model.Consumer;
import sophena.model.FacilityType;
import sophena.model.Pipe;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.Pump;

public class PartFactory implements EditPartFactory {

	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		AbstractGraphicalEditPart part = null;
		if (model instanceof Project)
			part = new ProjectPart();
		if (model instanceof Producer)
			part = new FacilityPart(FacilityType.PRODUCER);
		if (model instanceof Consumer)
			part = new FacilityPart(FacilityType.CONSUMER);
		if (model instanceof Pump)
			part = new FacilityPart(FacilityType.PUMP);
		if (model instanceof Pipe)
			part = new PipePart();
		part.setModel(model);
		return part;
	}

}
