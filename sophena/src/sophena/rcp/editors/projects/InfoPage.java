package sophena.rcp.editors.projects;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.rcp.utils.UI;

class InfoPage extends FormPage {

	private Text nameText;
	private Text descriptionText;
	private Text timeText;

	public InfoPage(ProjectEditor editor) {
		super(editor, "sophena.ProjectInfoPage", "#Projektinformationen");
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = UI.formHeader(managedForm, "#Project name");
		FormToolkit toolkit = managedForm.getToolkit();
		Composite body = UI.formBody(form, toolkit);
		createInfoSection(body, toolkit);
		form.reflow(true);
	}

	private void createInfoSection(Composite body, FormToolkit toolkit) {
		Composite composite = UI.formSection(body, toolkit,
				"#Projektinformationen");
		nameText = UI.formText(composite, toolkit, "#Name");
		descriptionText = UI.formMultiText(composite, toolkit, "#Beschreibung");
		timeText = UI.formText(composite, toolkit, "#Projektlaufzeit (Jahre)");
	}

}
