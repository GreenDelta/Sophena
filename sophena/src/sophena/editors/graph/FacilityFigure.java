package sophena.editors.graph;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.XYLayout;

public abstract class FacilityFigure extends Figure {

	public FacilityFigure() {
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);
		setOpaque(true);
	}

	public void setLabel(String label) {
		setToolTip(new Label(label));
	}

	@Override
	protected abstract void paintFigure(Graphics g);

}
