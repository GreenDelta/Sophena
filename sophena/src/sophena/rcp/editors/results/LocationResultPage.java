package sophena.rcp.editors.results;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.function.Supplier;

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
import org.eclipse.ui.forms.widgets.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import sophena.model.Project;
import sophena.rcp.M;
import sophena.rcp.utils.Rcp;
import sophena.rcp.utils.UI;

public class LocationResultPage extends FormPage {

	private Supplier<Project> proj;
	private ResultEditor editor;
	private FormToolkit toolkit;
	private Browser browser;

	public LocationResultPage(ResultEditor editor) {// ,

		super(editor, "sophena.LocationResultPage.html", "Standort");
		this.editor = editor;

	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, M.Location);
		toolkit = mform.getToolkit();
		Composite body = UI.formBody(form, toolkit);
		createBrowserSection(body);
		form.reflow(true);
	}

	private void createBrowserSection(Composite body) {
		Section s = UI.section(body, toolkit, "Karte");
		UI.gridData(s, true, true);
		Composite c = UI.sectionClient(s, toolkit);
		c.setLayout(new FillLayout());
		browser = new Browser(c, SWT.NONE);
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
		// new LocationChangeFn();
		InitData initData = new InitData();

		String json = new Gson().toJson(initData);
		try {
			browser.evaluate("init(" + json + ")");
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to initialize browser " + json, e);
		}
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

}