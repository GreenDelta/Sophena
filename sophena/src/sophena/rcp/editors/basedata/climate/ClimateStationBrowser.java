package sophena.rcp.editors.basedata.climate;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import javafx.concurrent.Worker.State;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import sophena.db.daos.WeatherStationDao;
import sophena.model.descriptors.WeatherStationDescriptor;
import sophena.rcp.App;
import sophena.rcp.utils.Rcp;

public class ClimateStationBrowser {

	private WebEngine webkit;

	private ClimateStationBrowser() {
	}

	public static void create(Composite parent) {
		new ClimateStationBrowser().render(parent);
	}

	private void render(Composite parent) {
		parent.setLayout(new FillLayout());
		FXCanvas fxCanvas = new FXCanvas(parent, SWT.NONE);
		fxCanvas.setLayout(new FillLayout());
		WebView view = new WebView();
		Scene scene = new Scene(view);
		fxCanvas.setScene(scene);
		webkit = view.getEngine();
		webkit.load(getUrl());
		webkit.getLoadWorker().stateProperty().addListener((v, old, newState) -> {
			if (newState != State.SUCCEEDED)
				return;
			initBrowser();
		});
	}

	private String getUrl() {
		String pageName = "WeatherStations.html";
		File dir = Rcp.getWorkspace();
		File f = new File(dir, pageName);
		try {
			if (!f.exists()) {
				InputStream is = getClass().getResourceAsStream(pageName);
				Files.copy(is, f.toPath());
			}
			return f.toURI().toURL().toString();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Could not get URL to location page", e);
			return "";
		}
	}

	private void initBrowser() {
		try {
			WeatherStationDao dao = new WeatherStationDao(App.getDb());
			List<WeatherStationDescriptor> list = dao.getDescriptors();
			String json = new Gson().toJson(list);
			webkit.executeScript("setData(" + json + ")");
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Failed to set browser data", e);
		}
	}
}