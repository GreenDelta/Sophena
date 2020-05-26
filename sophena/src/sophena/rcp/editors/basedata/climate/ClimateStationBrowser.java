package sophena.rcp.editors.basedata.climate;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import sophena.db.daos.WeatherStationDao;
import sophena.rcp.App;
import sophena.rcp.Workspace;
import sophena.rcp.utils.UI;

public class ClimateStationBrowser {

	private ClimateStationBrowser() {
	}

	public static void create(Composite parent) {
		new ClimateStationBrowser().render(parent);
	}

	private void render(Composite parent) {
		parent.setLayout(new FillLayout());
		var browser = new Browser(parent, SWT.NONE);
		browser.setJavascriptEnabled(true);
		UI.onLoaded(browser, getUrl(), () -> {
			var dao = new WeatherStationDao(App.getDb());
			var list = dao.getDescriptors();
			var json = new Gson().toJson(list);
			browser.execute("setData(" + json + ")");
		});
	}

	private String getUrl() {
		String pageName = "WeatherStations.html";
		File f = new File(Workspace.dir(), pageName);
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
}