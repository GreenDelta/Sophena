package sophena.rcp.editors;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.Dao;
import sophena.model.Manufacturer;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.UI;
import sophena.utils.Strings;

public class StartPage extends FormEditor {

	private static final String ID = "sophena.StartPage";
	private final static Logger log = LoggerFactory.getLogger(StartPage.class);

	public static void open() {
		Editors.open(new KeyEditorInput(ID, M.Welcome), ID);
	}

	@Override
	protected void addPages() {
		try {
			addPage(new Page());
		} catch (PartInitException e) {
			log.error("Error adding start page", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {

	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		setPartName(M.Welcome);
	}

	private class Page extends FormPage {

		public Page() {
			super(StartPage.this, "sophena.StartPage", M.Welcome);
		}

		@Override
		protected void createFormContent(IManagedForm mform) {
			var form = UI.formHeader(mform, M.HomePage);
			var tk = mform.getToolkit();
			var body = UI.formBody(form, tk);
			body.setLayout(new FillLayout());
			var browser = new Browser(body, SWT.NONE);
			browser.setText(createHtml());
			form.reflow(true);
		}

		private String createHtml() {

			// read the template
			String template;
			try (var stream = getClass().getResourceAsStream("StartPage.html");
					var reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
					var buff = new BufferedReader(reader)) {
				template = buff.lines()
						.collect(Collectors.joining("\n"));
			} catch (Exception e) {
				return "";
			}

			var manufs = new Dao<>(Manufacturer.class, App.getDb())
					.getAll()
					.stream()
					.filter(m -> Strings.notEmpty(m.logo))
					.sorted((m1, m2) -> Integer.compare(
							m1.sponsorOrder, m2.sponsorOrder))
					.collect(Collectors.toList());
			if (manufs.isEmpty())
				return template.replace("${logos}", "");

			// create the logo rows
			int i = 0;
			var logos = new StringBuilder("<h2>Mit Produkten von:</h2>")
					.append("<table><tbody><tr>");
			for (var m : manufs) {
				if (i > 0 && i % 4 == 0) {
					logos.append("</tr><tr>");
				}
				logos.append("<td><img src=\"")
						.append(m.logo)
						.append("\" alt=\"")
						.append(m.name)
						.append("\" width=\"130\" /></td>");
				i++;
			}
			logos.append("</tr></tbody></table>");
			return template.replace("${logos}", logos);
		}
	}
}
