package sophena.editors.graph;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import sophena.model.Pipe;
import sophena.model.Producer;
import sophena.model.Project;

public class PartFactory implements EditPartFactory {

	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		AbstractGraphicalEditPart part = null;
		if (model instanceof Project)
			part = new ProjectPart();
		if (model instanceof Producer)
			part = new ProducerPart();
		if (model instanceof Pipe)
			part = new PipePart();
		part.setModel(model);
		return part;
	}

}
