package sophena.editors.graph;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;

public class ConsumerFigure extends FacilityFigure {

	@Override
	protected void paintFigure(Graphics g) {
		g.setForegroundColor(ColorConstants.blue);
		g.setLineWidth(2);
		Point loc = getLocation();
		g.drawOval(loc.x + 3, loc.y + 3, 44, 44);
		g.setLineWidth(1);
		g.drawLine(loc.x + 17, loc.y + 15, loc.x + 27, loc.y + 35);
		g.drawLine(loc.x + 23, loc.y + 15, loc.x + 33, loc.y + 35);

	}

}
