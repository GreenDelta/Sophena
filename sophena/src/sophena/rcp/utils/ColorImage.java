package sophena.rcp.utils;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class ColorImage {

	private ImageRegistry reg = new ImageRegistry();
	private Display display;

	public ColorImage(Display display) {
		this.display = display;
	}

	public void dispose() {
		reg.dispose();
	}

	public Image get(int i) {
		String key = Integer.toString(i);
		Image img = reg.get(key);
		if(img != null)
			return img;
		img = new Image(display, 15, 15);
		Color color = Colors.getForChart(i);
		GC gc = new GC(img);
		gc.setBackground(color);
		gc.fillRectangle(2, 5, 11, 5);
		gc.dispose();
		reg.put(key, img);
		return img;
	}
}
