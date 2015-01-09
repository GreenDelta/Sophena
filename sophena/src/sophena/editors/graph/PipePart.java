package sophena.editors.graph;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionRouter;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;

public class PipePart extends AbstractConnectionEditPart {

	@Override
	protected IFigure createFigure() {
		// PolylineConnection con = (PolylineConnection) super.createFigure();
		// con.setLineWidth(3);
		PolylineConnection figure = new PolylineConnection();
		figure.setForegroundColor(ColorConstants.blue);
		figure.setLineWidth(5);
		figure.setConnectionRouter(ConnectionRouter.NULL);
		figure.setTargetDecoration(new PolygonDecoration());
		figure.setVisible(true);
		return figure;
	}

	@Override
	protected void createEditPolicies() {

	}
}
