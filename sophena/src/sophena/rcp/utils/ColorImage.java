package sophena.rcp.utils;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import sophena.rcp.colors.Colors;

public class ColorImage {

	private final ImageRegistry reg = new ImageRegistry();
	private final Display display;

	public ColorImage(Display display) {
		this.display = display;
	}

	public void dispose() {
		reg.dispose();
	}

	public Image get(Color color) {
		if (color == null)
			return get(Colors.getBlack());
		var key = Colors.toHex(color.getRGB());
		var img = reg.get(key);
		if (img != null)
			return img;
		img = makeImage(color);
		reg.put(key, img);
		return img;
	}

	public Image getRed() {
		return get(Colors.getSystemColor(SWT.COLOR_RED));
	}

	private Image makeImage(Color color) {
		var img = new Image(display, 15, 15);
		var gc = new GC(img);
		gc.setBackground(color);
		gc.fillRectangle(2, 5, 11, 5);
		gc.dispose();
		return img;
	}
}
