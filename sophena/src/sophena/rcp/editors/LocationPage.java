package sophena.rcp.editors;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import sophena.model.Location;
import sophena.model.Project;
import sophena.rcp.M;
import sophena.rcp.Workspace;
import sophena.rcp.utils.Rcp;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

public class LocationPage extends FormPage {

	private Supplier<Location> loc;
	private Supplier<Project> proj;
	private Editor editor;
	private FormToolkit toolkit;

	private Text latText;
	private Text lngText;
	private Browser browser;

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
		t(c, M.Name, init.name, s -> loc.get().name = s);
		t(c, M.Street, init.street, s -> loc.get().street = s);
		t(c, M.ZipCode, init.zipCode, s -> loc.get().zipCode = s);
		t(c, M.City, init.city, s -> loc.get().city = s);
		latText = d(c, "Breitengrad", init.latitude,
				d -> loc.get().latitude = d);
		lngText = d(c, "Längengrad", init.longitude,
				d -> loc.get().longitude = d);
	}

	private void t(Composite comp, String label, String initial,
			Consumer<String> fn) {
		Text t = UI.formText(comp, toolkit, label);
		Texts.on(t).init(initial).onChanged(s -> {
			fn.accept(s);
			editor.setDirty();
		});
	}

	private Text d(Composite comp, String label, Double initial,
			Consumer<Double> fn) {
		Text t = UI.formText(comp, toolkit, label);
		Texts.on(t).init(initial).onChanged(s -> {
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
		var section = UI.section(body, toolkit, "Karte");
		UI.gridData(section, true, true);
		var comp = UI.sectionClient(section, toolkit);
		browser = new Browser(comp, SWT.NONE);
		var url = Workspace.html(
				"LocationPage",
				() -> getClass().getResourceAsStream("LocationPage.html"));
		UI.onLoaded(browser, url, () -> {
			InitData initData = getInitialLocation();
			String json = new Gson().toJson(initData);
			browser.execute("init(" + json + ")");
		});
	}

	private InitData getInitialLocation() {
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
		return initData;
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
					browser.execute("removeMarker()");
				else {
					LatLng latlng = new LatLng();
					latlng.lat = l.latitude;
					latlng.lng = l.longitude;
					String json = new Gson().toJson(latlng);
					browser.execute("setLocation(" + json + ")");
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

	public class JSHandler {
		public void locationChanged(String json) {
			if (json == null)
				return;
			Rcp.runInUI("update texts", () -> {
				Gson gson = new Gson();
				LatLng latlng = gson.fromJson(json, LatLng.class);
				Texts.set(latText, Num.str(latlng.lat, 6));
				Texts.set(lngText, Num.str(latlng.lng, 6));
			});
		}
	}
}
