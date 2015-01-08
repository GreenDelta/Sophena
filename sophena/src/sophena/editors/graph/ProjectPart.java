package sophena.editors.graph;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import sophena.model.Project;

public class ProjectPart extends AbstractGraphicalEditPart {

	@Override
	protected IFigure createFigure() {
		return new ProjectFigure();
	}

	@Override
	protected void createEditPolicies() {
	}

	@Override
	protected void refreshVisuals() {
		Object model = getModel();
		if (!(model instanceof Project))
			return;
		Project project = (Project) model;
		ProjectFigure figure = (ProjectFigure) getFigure();
		figure.setLabel(project.getName());
	}

}
