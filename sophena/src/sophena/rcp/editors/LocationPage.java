package sophena.rcp.editors;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
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

import sophena.model.Location;
import sophena.rcp.M;
import sophena.rcp.utils.Rcp;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

public class LocationPage extends FormPage {

	private Supplier<Location> loc;
	private Editor editor;
	private FormToolkit toolkit;
	private Browser browser;

	public LocationPage(Editor editor, Supplier<Location> location) {
		super(editor, "sophena.LocationPage", M.Location);
		this.editor = editor;
		this.loc = location;
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
		Composite c = UI.formSection(body, toolkit, "Standord");
		Location init = loc.get();
		t(c, M.Name, init.name, (s) -> loc.get().name = s);
		t(c, M.Street, init.street, (s) -> loc.get().street = s);
		t(c, M.ZipCode, init.zipCode, (s) -> loc.get().zipCode = s);
		t(c, M.City, init.city, (s) -> loc.get().city = s);
		d(c, "Breitengrad", init.latitude, (d) -> loc.get().latitude = d);
		d(c, "Längengrad", init.longitude, (d) -> loc.get().longitude = d);
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

	private void d(Composite comp, String label, Double initial,
			Consumer<Double> fn) {
		Text t = UI.formText(comp, toolkit, label);
		Texts.on(t)
				.init(initial)
				.onChanged((s) -> {
					if (Texts.isEmpty(t)) {
						fn.accept(null);
						// delete marker
					} else {
						fn.accept(Texts.getDouble(t));
						// update marker
					}
					editor.setDirty();
				});
	}

	private void createBrowserSection(Composite body) {
		Section s = UI.section(body, toolkit, "Karte");
		UI.gridData(s, true, true);
		Composite c = UI.sectionClient(s, toolkit);
		c.setLayout(new FillLayout());
		browser = new Browser(c, SWT.NONE);
		browser.setJavascriptEnabled(true);
		browser.setUrl(getUrl());
		browser.addProgressListener(new ProgressListener() {
			@Override
			public void completed(ProgressEvent event) {
				browser.evaluate(
						"init({latlng: {lat: 48.88402, lng: 12.58334}, withMarker: true})");
			}

			@Override
			public void changed(ProgressEvent event) {
			}
		});

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
}
