package sophena.editors.graph.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;

public class ProjectFigure extends Figure {

	private Label label = new Label();
	private XYLayout layout = new XYLayout();

	public ProjectFigure() {
		layout = new XYLayout();
		setLayoutManager(layout);
		label.setForegroundColor(ColorConstants.blue);
		add(label);
		setConstraint(label, new Rectangle(5, 5, -1, -1));
		setForegroundColor(ColorConstants.black);
	}

	public void setLabel(String label) {
		this.label.setText(label);
	}

}
