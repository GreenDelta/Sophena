package sophena.rcp.editors.projects;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.model.Project;
import sophena.rcp.M;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class InfoPage extends FormPage {

	private ProjectEditor editor;

	public InfoPage(ProjectEditor editor) {
		super(editor, "sophena.ProjectInfoPage", "Projektinformationen");
		this.editor = editor;
	}

	private Project project() {
		return editor.getProject();
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, editor.getProject().getName());
		FormToolkit toolkit = mform.getToolkit();
		Composite body = UI.formBody(form, toolkit);
		createInfoSection(body, toolkit);
		form.reflow(true);
	}

	private void createInfoSection(Composite body, FormToolkit toolkit) {
		Composite composite = UI.formSection(body, toolkit,
				M.ProjectInformation);
		createNameText(toolkit, composite);
		createDescriptionText(toolkit, composite);
		createDurationText(toolkit, composite);
	}

	private void createNameText(FormToolkit toolkit, Composite composite) {
		Text t = UI.formText(composite, toolkit, M.Name);
		Texts.on(t)
				.init(project().getName())
				.required()
				.onChanged(() -> {
					project().setName(t.getText());
					editor.setDirty();
				});
	}

	private void createDescriptionText(FormToolkit toolkit, Composite composite) {
		Text t = UI.formMultiText(composite, toolkit, M.Description);
		Texts.on(t)
				.init(project().getDescription())
				.onChanged(() -> {
					project().setDescription(t.getText());
					editor.setDirty();
				});
	}

	private void createDurationText(FormToolkit toolkit, Composite composite) {
		Text t = UI.formText(composite, toolkit, M.ProjectDurationYears);
		Texts.on(t)
				.init(project().getProjectDuration())
				.required()
				.integer()
				.onChanged(() -> {
					project().setProjectDuration(Texts.getInt(t));
					editor.setDirty();
				});
	}
}
