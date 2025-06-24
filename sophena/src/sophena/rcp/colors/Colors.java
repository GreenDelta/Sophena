package sophena.rcp.colors;

import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.utils.Strings;

/**
 * Managed SWT colors: the colors are created on demand and disposed when the
 * application is closed.
 */
public class Colors {

	private static final Logger log = LoggerFactory.getLogger(Colors.class);
	private static final HashMap<RGB, Color> createdColors = new HashMap<>();
	private static final Display display;

	static {
		display = PlatformUI.getWorkbench().getDisplay();
		PlatformUI.getWorkbench().addWorkbenchListener(new ShutDown());
	}

	private static final RGB[] chartColors = {
			new RGB(41, 111, 196),
			new RGB(255, 201, 35),
			new RGB(82, 168, 77),
			new RGB(132, 76, 173),
			new RGB(127, 183, 229),
			new RGB(255, 137, 0),
			new RGB(128, 0, 128),
			new RGB(135, 76, 63),
			new RGB(252, 255, 100),
			new RGB(0, 177, 241),
			new RGB(112, 187, 40),
			new RGB(18, 89, 133),
			new RGB(226, 0, 115),
			new RGB(255, 255, 85),
			new RGB(218, 0, 24),
			new RGB(0, 111, 154),
			new RGB(255, 153, 0)
	};

	public static Color getErrorColor() {
		RGB rgb = new RGB(255, 180, 180);
		return of(rgb);
	}

	public static Color of(RGB rgb) {
		Color color = createdColors.get(rgb);
		if (color == null || color.isDisposed()) {
			color = new Color(display, rgb);
			createdColors.put(rgb, color);
		}
		return color;
	}

	public static Color of(int r, int g, int b) {
		RGB rgb = new RGB(r, g, b);
		return of(rgb);
	}

	public static Color of(String hex) {
		var rgb = rgbOf(hex);
		return of(rgb);
	}

	public static RGB rgbOf(String hex) {
		if (Strings.nullOrEmpty(hex))
			return new RGB(0, 0, 0);
		var s = hex.replace("#", "").trim();
		if (s.length() < 6)
			return new RGB(0, 0, 0);
		int r = Integer.valueOf(s.substring(0, 2), 16);
		int g = Integer.valueOf(s.substring(2, 4), 16);
		int b = Integer.valueOf(s.substring(4, 6), 16);
		return new RGB(r, g, b);
	}

	public static String toHex(RGB rgb) {
		if (rgb == null)
			return "#000000";
		var xs = List.of(
				Integer.toHexString(rgb.red),
				Integer.toHexString(rgb.green),
				Integer.toHexString(rgb.blue));
		var s = new StringBuilder(7);
		s.append("#");
		for (var x : xs) {
			if (x.length() == 1) {
				s.append('0').append(x);
			} else {
				s.append(x, 0, 2);
			}
		}
		return s.toString();
	}

	public static Color getWhite() {
		return display.getSystemColor(SWT.COLOR_WHITE);
	}

	/**
	 * Returns the defined chart color for the given index. If the index is out
	 * of the range of the pre-defined colors, a random color is returned.
	 */
	public static Color getForChart(int idx) {
		return Colors.of(getRgbForChart(idx));
	}

	/**
	 * Returns the defined chart color for the given index. If the index is out
	 * of the range of the pre-defined colors, a random color is returned.
	 */
	public static RGB getRgbForChart(int idx) {
		return idx < 0 || idx >= chartColors.length
				? next(idx)
				: chartColors[idx];
	}

	private static RGB next(int idx) {
		if (idx == 0)
			return new RGB(255, 255, 255);
		int blue = 255 / Math.abs(idx);
		int red = 255 - blue;
		int green = (blue + red) / 2;
		return new RGB(red, green, blue);
	}

	public static Color getChartBlue() {
		return of(68, 114, 162);
	}

	public static Color getChartRed() {
		return of(180, 26, 30);
	}

	public static Color getDarkGray() {
		return display.getSystemColor(SWT.COLOR_DARK_GRAY);
	}

	public static Color getLinkBlue() {
		return of(25, 76, 127);
	}

	public static Color getGray() {
		return of(128, 128, 128);
	}

	public static Color getBlack() {
		return display.getSystemColor(SWT.COLOR_BLACK);
	}

	public static Color getSystemColor(int swt) {
		return display.getSystemColor(swt);
	}

	public static Color forRequiredField() {
		return of(255, 255, 220);
	}

	public static Color forCalculatedField() {
		return of(230, 230, 233);
	}

	public static Color forModifiedDefault() {
		return of(225, 213, 232);
	}


	private static class ShutDown implements IWorkbenchListener {
		@Override
		public boolean preShutdown(IWorkbench workbench, boolean forced) {
			return true;
		}

		@Override
		public void postShutdown(IWorkbench workbench) {
			log.trace("dispose {} created colors", createdColors.size());
			for (Color color : createdColors.values()) {
				if (!color.isDisposed())
					color.dispose();
			}
		}
	}
}
