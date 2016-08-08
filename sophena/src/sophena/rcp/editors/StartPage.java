package sophena.rcp.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import sophena.rcp.M;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.UI;

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

		private FormToolkit toolkit;
		private WebEngine webkit;

		public Page() {
			super(StartPage.this, "sophena.StartPage", M.Welcome);
		}

		@Override
		protected void createFormContent(IManagedForm mform) {
			ScrolledForm form = UI.formHeader(mform, M.HomePage);
			toolkit = mform.getToolkit();
			Composite body = UI.formBody(form, toolkit);
			createBrowserSection(body);
			form.reflow(true);
		}

		private void createBrowserSection(Composite body) {
			body.setLayout(new FillLayout());
			FXCanvas fxCanvas = new FXCanvas(body, SWT.NONE);
			fxCanvas.setLayout(new FillLayout());
			WebView view = new WebView();
			Scene scene = new Scene(view);
			fxCanvas.setScene(scene);
			webkit = view.getEngine();
			webkit.load(getClass().getResource("Start.html").toExternalForm());
		}
	}

}
