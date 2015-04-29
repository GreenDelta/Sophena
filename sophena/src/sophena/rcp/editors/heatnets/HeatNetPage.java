package sophena.rcp.editors.heatnets;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import sophena.model.HeatNet;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;

class HeatNetPage extends FormPage {

	private HeatNetEditor editor;

	public HeatNetPage(HeatNetEditor editor) {
		super(editor, "sophena.HeatNetPage", "Wärmeverteilung");
		this.editor = editor;
	}

	private HeatNet heatNet() {
		return editor.getHeatNet();
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		super.createFormContent(mform);
		ScrolledForm form = UI.formHeader(mform, "Wärmeverteilung");
		FormToolkit toolkit = mform.getToolkit();
		Composite body = UI.formBody(form, toolkit);
		new HeatNetSection(editor).create(body, toolkit);
		createInterruptionSection(body, toolkit);
		createComponentSection(body, toolkit);
		form.reflow(true);
	}

	private void createInterruptionSection(Composite body, FormToolkit toolkit) {
		Composite composite = UI.formSection(body, toolkit,
				"Wärmenetz - Unterbrechung");
		UI.gridLayout(composite, 2);
		UI.formLabel(composite, toolkit, M.Start);
		DateTime start = new DateTime(composite, SWT.DATE | SWT.DROP_DOWN);
		UI.formLabel(composite, toolkit, M.End);
		DateTime end = new DateTime(composite, SWT.DATE | SWT.DROP_DOWN);
	}

	private void createComponentSection(Composite body, FormToolkit toolkit) {
		Section section = UI.section(body, toolkit, "Wärmenetz - Komponenten");
		Composite composite = UI.sectionClient(section, toolkit);
		UI.gridLayout(composite, 2);
		Tables.createViewer(composite, "Komponente", "Anzahl");
		Action add = Actions.create("Neue Komponente", Images.ADD_16.des(),
				() -> {
					NetComponentWizard.open(heatNet());
				});
		Action remove = Actions.create(M.Remove, Images.DELETE_16.des(),
				() -> {

				});
		Actions.bind(section, add, remove);
	}
}
