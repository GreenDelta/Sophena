package sophena.editors.graph;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;

public class ProducerFigure extends Figure {

	public ProducerFigure() {
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);
		setOpaque(true);
	}

	public void setLabel(String label) {
		setToolTip(new Label(label));
	}

	@Override
	protected void paintFigure(Graphics g) {
		g.setForegroundColor(ColorConstants.blue);
		g.setLineWidth(2);
		Point loc = getLocation();
		g.drawOval(loc.x + 3, loc.y + 3, 44, 44);
		g.setLineWidth(1);
		g.setForegroundColor(ColorConstants.red);
		g.drawLine(loc.x + 15, loc.y + 15, loc.x + 35, loc.y + 15);
		g.drawLine(loc.x + 35, loc.y + 15, loc.x + 20, loc.y + 25);
		g.drawLine(loc.x + 20, loc.y + 25, loc.x + 35, loc.y + 35);
		g.drawLine(loc.x + 35, loc.y + 35, loc.x + 15, loc.y + 35);

	}
}
