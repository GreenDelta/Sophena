package sophena.rcp.editors.results.single;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.calc.ProjectResult;
import sophena.rcp.utils.UI;

public class LogPage extends FormPage {

	private ProjectResult result;

	public LogPage(ResultEditor editor) {
		super(editor, "sophena.LogPage", "Berechnungsdetails");
		this.result = editor.result;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Berechnungsdetails");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		Text text = tk.createText(body, null, SWT.BORDER | SWT.V_SCROLL
				| SWT.WRAP | SWT.MULTI);
		UI.gridData(text, true, true);
		text.setFont(JFaceResources.getTextFont());
		text.setText(result.calcLog.toString());
		form.reflow(true);
	}

}
