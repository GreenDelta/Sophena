package sophena.rcp.editors.projects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.calc.ProjectCalculator;
import sophena.calc.ProjectResult;
import sophena.rcp.editors.LoadCurveSection;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.UI;

class ResultPage extends FormPage {

	private ProjectEditor editor;

	public ResultPage(ProjectEditor editor) {
		super(editor, "sophena.ProjectResultPage", "Ergebnisse");
		this.editor = editor;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Ergebnisse");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		Button button = tk.createButton(body, "Berechnen", SWT.NONE);
		LoadCurveSection loadCurveSection = new LoadCurveSection(body, tk);
		loadCurveSection.setSorted(false);
		Controls.onSelect(button, (e) -> {
			ProjectResult r = ProjectCalculator.calculate(editor.getProject());
			loadCurveSection.setData(r.getLoadCurve());
		});
	}
}
