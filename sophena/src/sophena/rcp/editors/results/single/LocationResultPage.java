package sophena.rcp.editors.results.single;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import sophena.model.Consumer;
import sophena.model.Location;
import sophena.rcp.M;
import sophena.rcp.utils.Rcp;
import sophena.rcp.utils.UI;

public class LocationResultPage extends FormPage {

	private ResultEditor editor;
	private FormToolkit toolkit;
	private Browser browser;

	public LocationResultPage(ResultEditor editor) {
		super(editor, "sophena.LocationResultPage.html", M.Locations);
		this.editor = editor;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, M.Locations);
		toolkit = mform.getToolkit();
		Composite body = UI.formBody(form, toolkit);
		createBrowser(body);
		form.reflow(true);
	}

	private void createBrowser(Composite body) {
		body.setLayout(new FillLayout());
		browser = new Browser(body, SWT.NONE);
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
		String pageName = "LocationResultPage.html";
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
		String json = createModel();
		try {
			System.out.println(json);
			browser.evaluate("setData(" + json + ")");
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to initialize browser " + json, e);
		}
	}

	private String createModel() {
		if (editor.project == null)
			return "{}";
		MapData data = new MapData();
		int points = 0;
		double latSum = 0;
		double lngSum = 0;
		for (Consumer c : editor.project.consumers) {
			Location loc = c.location;
			if (loc == null || loc.latitude == null || loc.longitude == null)
				continue;
			points++;
			latSum += loc.latitude;
			lngSum += loc.longitude;
			Marker marker = new Marker();
			marker.title = c.name;
			marker.latlng = new LatLng(loc.latitude, loc.longitude);
			data.markers.add(marker);
		}
		if (points == 0)
			data.center = new LatLng(48.884, 12.583);
		else
			data.center = new LatLng(latSum / points, lngSum / points);
		return new Gson().toJson(data);
	}

	@SuppressWarnings("unused")
	private class MapData {
		LatLng center;
		List<Marker> markers = new ArrayList<>();
	}

	@SuppressWarnings("unused")
	private class Marker {
		String title;
		LatLng latlng;
	}

	@SuppressWarnings("unused")
	private class LatLng {
		double lat;
		double lng;

		LatLng(double lat, double lng) {
			this.lat = lat;
			this.lng = lng;
		}
	}

}