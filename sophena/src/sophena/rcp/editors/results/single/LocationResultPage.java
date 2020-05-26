package sophena.rcp.editors.results.single;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;

import com.google.gson.Gson;

import sophena.model.Consumer;
import sophena.model.Location;
import sophena.rcp.M;
import sophena.rcp.Workspace;
import sophena.rcp.utils.UI;

public class LocationResultPage extends FormPage {

	private ResultEditor editor;

	public LocationResultPage(ResultEditor editor) {
		super(editor, "sophena.LocationResultPage.html", M.Locations);
		this.editor = editor;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		var form = UI.formHeader(mform, M.Locations);
		var tk = mform.getToolkit();
		var body = UI.formBody(form, tk);
		body.setLayout(new FillLayout());
		var browser = new Browser(body, SWT.NONE);
		var url = Workspace.html("LocationResultPage",
				() -> getClass().getResourceAsStream(
						"LocationResultPage.html"));
		UI.onLoaded(browser, url, () -> {
			String json = createModel();
			browser.execute("setData(" + json + ")");
		});
		form.reflow(true);
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