package sophena.rcp.charts;

import java.io.File;
import java.util.function.Supplier;

import org.eclipse.jface.action.Action;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swtchart.Chart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.rcp.Icon;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.Popup;
import sophena.rcp.utils.Rcp;
import sophena.utils.Ref;

public class ImageExport extends Action {

	private final Supplier<Image> image;
	private String defaultName;

	public ImageExport(String defaultName, Supplier<Image> image) {
		this.defaultName = defaultName;
		this.image = image;
		setText("Als Bild speichern");
		setImageDescriptor(Icon.CAMERA_16.des());
	}

	public static ImageExport forChart(String defaultName,
			Supplier<Chart> chart) {
		Supplier<Image> image = () -> {
			Chart c = chart.get();
			if (c == null)
				return null;
			Rectangle bounds = c.getBounds();
			Image img = new Image(c.getDisplay(), bounds.width, bounds.height);
			GC gc = new GC(img);
			c.print(gc);
			gc.dispose();
			return img;
		};
		return new ImageExport(defaultName, image);
	}

	public static ImageExport forXYGraph(String defaultName,
			Supplier<XYGraph> chart) {
		return new ImageExport(defaultName, () -> {
			if (chart == null)
				return null;
			XYGraph g = chart.get();
			if (g == null)
				return null;
			return g.getImage();
		});
	}

	@Override
	public void run() {
		if (image == null || image.get() == null)
			return;
		File file = FileChooser.save(defaultName, "*.jpg", "*.png");
		if (file == null)
			return;
		ImageData data = image.get().getImageData();
		Ref<Throwable> err = new Ref<>();
		Rcp.run("Speichere Bild",
				() -> doIt(data, file, err),
				() -> showMessage(err));
	}

	private void doIt(ImageData data, File file, Ref<Throwable> err) {
		try {
			String path = file.getAbsolutePath();
			int format = path.endsWith(".jpg")
					? SWT.IMAGE_JPEG : SWT.IMAGE_PNG;
			ImageLoader loader = new ImageLoader();
			loader.data = new ImageData[] { data };
			loader.save(path, format);
		} catch (Exception e) {
			err.set(e);
		}
	}

	private void showMessage(Ref<Throwable> err) {
		if (err.get() == null) {
			Popup.showInfo("Bild wurde gespeichert");
		} else {
			Popup.showError("Bild konnte nicht gespeichert werden");
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Failed to export image", err.get());
		}
	}
}