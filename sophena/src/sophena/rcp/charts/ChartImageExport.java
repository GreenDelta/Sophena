package sophena.rcp.charts;

import java.io.File;
import java.util.function.Supplier;

import org.eclipse.jface.action.Action;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.rcp.Icon;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.Popup;
import sophena.rcp.utils.Rcp;
import sophena.utils.Ref;

public class ChartImageExport extends Action {

	private final Supplier<XYGraph> chart;
	private String defaultName;

	public ChartImageExport(String defaultName, Supplier<XYGraph> chart) {
		this.defaultName = defaultName;
		this.chart = chart;
		setText("Als Bild speichern");
		setImageDescriptor(Icon.CAMERA_16.des());
	}

	@Override
	public void run() {
		if (chart == null || chart.get() == null)
			return;
		File file = FileChooser.saveFile(defaultName, "*.jpg", "*.png");
		if (file == null)
			return;
		ImageData data = chart.get().getImage().getImageData();
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