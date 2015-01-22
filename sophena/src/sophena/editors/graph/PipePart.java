package sophena.editors.graph;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;

public class PipePart extends AbstractConnectionEditPart {

	@Override
	protected IFigure createFigure() {
		PolylineConnection con = (PolylineConnection) super.createFigure();
		con.setLineWidth(2);
		con.setForegroundColor(ColorConstants.blue);
		return con;
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE,
				new ConnectionEndpointEditPolicy());
		installEditPolicy(EditPolicy.CONNECTION_ROLE, new PipeDeletePolicy());
	}

}
