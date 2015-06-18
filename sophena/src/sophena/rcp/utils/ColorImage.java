package sophena.rcp.utils;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
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
		if (img != null)
			return img;
		Color color = Colors.getForChart(i);
		img = makeImage(color);
		reg.put(key, img);
		return img;
	}

	public Image getRed() {
		Image img = reg.get("red");
		if (img != null)
			return img;
		Color color = Colors.getSystemColor(SWT.COLOR_RED);
		img = makeImage(color);
		reg.put("red", img);
		return img;
	}

	private Image makeImage(Color color) {
		Image img;
		img = new Image(display, 15, 15);
		GC gc = new GC(img);
		gc.setBackground(color);
		gc.fillRectangle(2, 5, 11, 5);
		gc.dispose();
		return img;
	}
}
