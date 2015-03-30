package sophena.rcp.editors.projects;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.wizards.NetComponentWizard;

class InfoPage extends FormPage {

	private ProjectEditor editor;

	public InfoPage(ProjectEditor editor) {
		super(editor, "sophena.ProjectInfoPage", "#Projektinformationen");
		this.editor = editor;
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = UI.formHeader(managedForm, editor.getProject()
				.getName());
		FormToolkit toolkit = managedForm.getToolkit();
		Composite body = UI.formBody(form, toolkit);
		createInfoSection(body, toolkit);
		createHeatNetSection(body, toolkit);
		createInterruptionSection(body, toolkit);
		createComponentSection(body, toolkit);
		form.reflow(true);
	}

	private void createInfoSection(Composite body, FormToolkit toolkit) {
		Composite composite = UI.formSection(body, toolkit,
				M.ProjectInformation);
		Text nameText = UI.formText(composite, toolkit, M.Name);
		Text descriptionText = UI.formMultiText(composite, toolkit,
				M.Description);
		Text timeText = UI.formText(composite, toolkit, M.ProjectDurationYears);
	}

	private void createHeatNetSection(Composite body, FormToolkit toolkit) {
		Composite composite = UI.formSection(body, toolkit,
				M.HeatingNetwork);
		UI.gridLayout(composite, 3);
		UI.formText(composite, toolkit, "#Länge");
		UI.formLabel(composite, toolkit, "m");
		UI.formText(composite, toolkit, "#Verlustleistung");
		UI.formLabel(composite, toolkit, "W/m");
		UI.formText(composite, toolkit, "#Gleichzeitigkeitsfaktor");
		UI.formLabel(composite, toolkit, "");
		UI.formText(composite, toolkit, "Vorlauftemperatur");
		UI.formLabel(composite, toolkit, "°C");
		UI.formText(composite, toolkit, "Rücklauflauftemperatur");
		UI.formLabel(composite, toolkit, "°C");
	}

	private void createInterruptionSection(Composite body, FormToolkit toolkit) {
		Composite composite = UI.formSection(body, toolkit,
				"#Wärmenetz - Unterbrechung");
		UI.gridLayout(composite, 2);
		UI.formLabel(composite, toolkit, M.Start);
		DateTime start = new DateTime(composite, SWT.DATE | SWT.DROP_DOWN);
		UI.formLabel(composite, toolkit, M.End);
		DateTime end = new DateTime(composite, SWT.DATE | SWT.DROP_DOWN);
	}

	private void createComponentSection(Composite body, FormToolkit toolkit) {
		Section section = UI.section(body, toolkit, "#Wärmenetz - Komponenten");
		Composite composite = UI.sectionClient(section, toolkit);
		UI.gridLayout(composite, 2);
		Tables.createViewer(composite, "#Komponente", "#Anzahl");
		Action add = Actions.create("#Neue Komponente", Images.ADD_16.des(),
				() -> {
					NetComponentWizard.open(editor.getProject());
				});
		Action remove = Actions.create(M.Remove, Images.DELETE_16.des(),
				() -> {

				});
		Actions.bind(section, add, remove);
	}

}