package sophena.editors.graph;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import sophena.model.Producer;

public class ProducerPart extends AbstractGraphicalEditPart {

	@Override
	protected IFigure createFigure() {
		return new ProducerFigure();
	}

	@Override
	protected void createEditPolicies() {
	}

	@Override
	protected void refreshVisuals() {
		ProducerFigure figure = (ProducerFigure) getFigure();
		Producer producer = (Producer) getModel();
		figure.setLabel(producer.getName());
		figure.getParent().setConstraint(figure,
				new Rectangle(100, 100, 200, 200));
	}

}
