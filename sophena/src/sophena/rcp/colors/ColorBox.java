package sophena.rcp.colors;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.rcp.utils.UI;

class ColorBox {

	private static final RGB DEFAULT = new RGB(90, 90, 100);

	private final Composite comp;
	private Color color;
	private Color hover;
	private Consumer<RGB> onChange;

	private ColorBox(String label, Composite parent, FormToolkit tk) {
		var root = tk.createComposite(parent);
		UI.fillHorizontal(root);
		UI.gridLayout(root, 1);
		var text = tk.createLabel(root, label);
		UI.gridData(text, false, false).horizontalAlignment = SWT.CENTER;

		comp = tk.createComposite(root);
		var gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.widthHint = 25;
		gd.heightHint = 25;
		gd.minimumWidth = 25;
		gd.minimumHeight = 25;
		comp.setLayoutData(gd);

		var cursor = new Cursor(comp.getDisplay(), SWT.CURSOR_HAND);
		comp.setCursor(cursor);
		comp.addDisposeListener(e -> {
			cursor.dispose();
			disposeColors();
		});

		comp.addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseEnter(MouseEvent e) {
				comp.setBackground(hover);
			}

			@Override
			public void mouseExit(MouseEvent e) {
				comp.setBackground(color);
			}
		});

		comp.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				var dialog = new ColorDialog(comp.getShell());
				dialog.setText("Farbe auswählen");
				dialog.setRGB(color.getRGB());
				var rgb = dialog.open();
				if (rgb == null)
					return;
				setColor(rgb);
				if (onChange != null) {
					onChange.accept(rgb);
				}
			}
		});

		setColor(DEFAULT);
	}

	static ColorBox of(String label, Composite parent, FormToolkit tk) {
		return new ColorBox(label, parent, tk);
	}

	ColorBox onChange(Consumer<RGB> fn) {
		this.onChange = fn;
		return this;
	}

	ColorBox setColor(RGB rgb) {
		if (rgb == null)
			return this;
		disposeColors();
		color = new Color(comp.getDisplay(), rgb);
		comp.setBackground(color);
		var hsb = rgb.getHSB();
		var hoverRGB = new RGB(hsb[0], 0.1f, 0.9f);
		hover = new Color(comp.getDisplay(), hoverRGB);
		return this;
	}

	private void disposeColors() {
		if (color != null && !color.isDisposed()) {
			color.dispose();
			color = null;
		}
		if (hover != null && !hover.isDisposed()) {
			hover.dispose();
			hover = null;
		}
	}

}
