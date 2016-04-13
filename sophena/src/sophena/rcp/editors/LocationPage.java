package sophena.rcp.editors;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import javafx.concurrent.Worker.State;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import sophena.model.Location;
import sophena.model.Project;
import sophena.rcp.M;
import sophena.rcp.utils.Rcp;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

public class LocationPage extends FormPage {

	private Supplier<Location> loc;
	private Supplier<Project> proj;
	private Editor editor;
	private FormToolkit toolkit;
	private Browser browser;

	private Text latText;
	private Text lngText;

	public LocationPage(Editor editor, Supplier<Location> location,
			Supplier<Project> proj) {
		super(editor, "sophena.LocationPage", M.Location);
		this.editor = editor;
		this.loc = location;
		this.proj = proj;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, M.Location);
		toolkit = mform.getToolkit();
		Composite body = UI.formBody(form, toolkit);
		createAddressSection(body);
		createBrowserSection(body);
		form.reflow(true);
	}

	private void createAddressSection(Composite body) {
		Composite c = UI.formSection(body, toolkit, M.Location);
		Location init = loc.get();
		t(c, M.Name, init.name, (s) -> loc.get().name = s);
		t(c, M.Street, init.street, (s) -> loc.get().street = s);
		t(c, M.ZipCode, init.zipCode, (s) -> loc.get().zipCode = s);
		t(c, M.City, init.city, (s) -> loc.get().city = s);
		latText = d(c, "Breitengrad", init.latitude,
				(d) -> loc.get().latitude = d);
		lngText = d(c, "Längengrad", init.longitude,
				(d) -> loc.get().longitude = d);
	}

	private void t(Composite comp, String label, String initial,
			Consumer<String> fn) {
		Text t = UI.formText(comp, toolkit, label);
		Texts.on(t)
				.init(initial)
				.onChanged((s) -> {
					fn.accept(s);
					editor.setDirty();
				});
	}

	private Text d(Composite comp, String label, Double initial,
			Consumer<Double> fn) {
		Text t = UI.formText(comp, toolkit, label);
		Texts.on(t)
				.init(initial)
				.onChanged((s) -> {
					if (Texts.isEmpty(t)) {
						fn.accept(null);
						updateMarker();
					} else {
						fn.accept(Texts.getDouble(t));
						updateMarker();
					}
					editor.setDirty();
				});
		return t;
	}

	private void createBrowserSection(Composite body) {
		Section s = UI.section(body, toolkit, "Karte");
		UI.gridData(s, true, true);
		Composite c = UI.sectionClient(s, toolkit);
		c.setLayout(new FillLayout());

		FXCanvas fxCanvas = new FXCanvas(c, SWT.NONE);
		fxCanvas.setLayout(new FillLayout());
		WebView view = new WebView();
		Scene scene = new Scene(view);
		fxCanvas.setScene(scene);
		WebEngine engine = view.getEngine();
		engine.load(getUrl());
		engine.getLoadWorker().stateProperty().addListener((v, old, newState) -> {
			if (newState == State.SUCCEEDED) {
				initBrowser(engine);
			}
		});

		/*
		 * browser = new Browser(c, SWT.NONE);
		 * browser.setJavascriptEnabled(true); browser.setUrl(getUrl());
		 * browser.addProgressListener(new ProgressAdapter() {
		 * 
		 * @Override public void completed(ProgressEvent event) { initBrowser();
		 * } });
		 */
	}

	private String getUrl() {
		String pageName = "LocationPage.html";
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

	private void initBrowser(WebEngine engine) {
		// new LocationChangeFn();
		InitData initData = new InitData();
		Location location = loc.get();
		if (location.latitude != null && location.longitude != null) {
			initData.latlng.lat = location.latitude;
			initData.latlng.lng = location.longitude;
			initData.withMarker = true;
		} else {
			LatLng initLoc = findInitialLocation();
			initData.latlng.lat = initLoc.lat;
			initData.latlng.lng = initLoc.lng;
			initData.withMarker = false;
		}
		String json = new Gson().toJson(initData);
		try {
			engine.executeScript("init(" + json + ")");
			// browser.evaluate("init(" + json + ")");
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to initialize browser " + json, e);
		}
	}

	private LatLng findInitialLocation() {
		LatLng init = new LatLng();
		int count = 0;
		Project p = proj.get();
		for (sophena.model.Consumer c : p.consumers) {
			Location l = c.location;
			if (l == null || l.latitude == null || l.longitude == null)
				continue;
			init.lat += l.latitude;
			init.lng += l.longitude;
			count++;
		}
		if (count == 0) {
			init.lat = 48.884;
			init.lng = 12.583;
		} else {
			init.lat /= count;
			init.lng /= count;
		}
		return init;
	}

	private void updateMarker() {
		Location l = loc.get();
		Rcp.runInUI("update marker", () -> {
			try {
				if (l == null || l.latitude == null || l.longitude == null)
					browser.evaluate("removeMarker()");
				else {
					LatLng latlng = new LatLng();
					latlng.lat = l.latitude;
					latlng.lng = l.longitude;
					String json = new Gson().toJson(latlng);
					browser.evaluate("setLocation(" + json + ")");
				}
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(getClass());
				log.error("failed to update marker", e);
			}
		});

	}

	@SuppressWarnings("unused")
	private class InitData {
		boolean withMarker;
		final LatLng latlng = new LatLng();
	}

	private class LatLng {
		double lat;
		double lng;
	}

	private class LocationChangeFn extends BrowserFunction {

		public LocationChangeFn() {
			super(browser, "locationChanged");
		}

		@Override
		public Object function(Object[] args) {
			if (args == null || args.length == 0 || args[0] == null)
				return null;
			Rcp.runInUI("update texts", () -> {
				String json = args[0].toString();
				Gson gson = new Gson();
				LatLng latlng = gson.fromJson(json, LatLng.class);
				Texts.set(latText, latlng.lat);
				Texts.set(lngText, latlng.lng);
			});
			return null;
		}
	}
}
