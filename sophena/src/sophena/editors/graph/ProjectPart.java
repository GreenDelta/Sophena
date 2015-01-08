package sophena.editors.graph;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import sophena.model.Project;

public class ProjectPart extends AbstractGraphicalEditPart {

	@Override
	protected IFigure createFigure() {
		return new ProjectFigure();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ProjectEditPolicy());
	}

	@Override
	protected void refreshVisuals() {
		Project project = (Project) getModel();
		ProjectFigure figure = (ProjectFigure) getFigure();
		figure.setLabel(project.getName());
		for (Object child : getChildren()) {
			if (child instanceof ProducerPart)
				((ProducerPart) child).refreshVisuals();
		}
	}

	@Override
	protected List<?> getModelChildren() {
		Project project = (Project) getModel();
		return project.getProducers();
	}
}
