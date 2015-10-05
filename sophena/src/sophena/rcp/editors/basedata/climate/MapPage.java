package sophena.rcp.editors.basedata.climate;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import sophena.db.daos.WeatherStationDao;
import sophena.model.descriptors.WeatherStationDescriptor;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.utils.Rcp;
import sophena.rcp.utils.UI;

class MapPage extends FormPage {

	private Browser browser;

	public MapPage(ClimateDataEditor editor) {
		super(editor, "climate.MapPage", "Karte");
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, M.Location);
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		createBrowserSection(body);
		form.reflow(true);
	}

	private void createBrowserSection(Composite body) {
		browser = new Browser(body, SWT.NONE);
		UI.gridData(browser, true, true);
		browser.setJavascriptEnabled(true);
		browser.setUrl(getUrl());
		browser.addProgressListener(new ProgressAdapter() {
			@Override
			public void completed(ProgressEvent event) {
				initBrowser();
			}
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
			browser.evaluate("setData(" + json + ")");
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Failed to set browser data", e);
		}
	}

}
