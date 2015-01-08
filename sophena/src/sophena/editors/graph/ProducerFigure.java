package sophena.editors.graph;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;

public class ProducerFigure extends Figure {

	private Label label = new Label();

	public ProducerFigure() {
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);
		label.setForegroundColor(ColorConstants.darkGray);
		add(label);
		setConstraint(label, new Rectangle(5, 17, -1, -1));
		setBackgroundColor(ColorConstants.lightBlue);
		setOpaque(true);
	}

	public void setLabel(String label) {
		this.label.setText(label);
	}

}
