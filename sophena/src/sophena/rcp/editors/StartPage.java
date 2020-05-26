package sophena.rcp.editors;

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

import sophena.rcp.M;
import sophena.rcp.Workspace;
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
			browser.setJavascriptEnabled(true);
			var url = Workspace.html(
					"Home",
					() -> getClass().getResourceAsStream("Start.html"));
			browser.setUrl(url);
			form.reflow(true);
		}
	}
}
