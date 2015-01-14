package sophena.editors.graph.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;

public class ProducerFigure extends FacilityFigure {

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
