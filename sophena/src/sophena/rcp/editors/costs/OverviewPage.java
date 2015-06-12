package sophena.rcp.editors.costs;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
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

class OverviewPage extends FormPage {

	private CostEditor editor;

	public OverviewPage(CostEditor editor) {
		super(editor, "sophena.CostOverviewPage", "Kostenübersicht");
		this.editor = editor;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Kostenübersicht");
		FormToolkit toolkit = mform.getToolkit();
		Composite body = UI.formBody(form, toolkit);
		createSection("Kessel", body, toolkit);
		createSection("KWK-Anlagen", body, toolkit);
		createSection("Kesselzubehör", body, toolkit);
		createSection("Pufferspeicher", body, toolkit);
		createSection("Wärmerückgewinnung", body, toolkit);
		createSection("Rauchgasreinigung", body, toolkit);
		createSection("Heizhaus-Technik", body, toolkit);
		createSection("Gebäude", body, toolkit);
		createSection("Wärmenetz-Technik", body, toolkit);
		createSection("Wärmenetz-Bau", body, toolkit);
		createSection("Planung", body, toolkit);
		form.reflow(true);
	}

	private void createSection(String label, Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, label);
		Composite composite = UI.sectionClient(section, tk);
		TableViewer table = createTable(composite);
		Action add = Actions.create(M.Add, Images.ADD_16.des(), ()->{});
		Action remove = Actions.create(M.Remove, Images.DELETE_16.des(),
				()->{});
		Action edit = Actions.create(M.Edit, Images.EDIT_16.des(),
				()->{});
		Actions.bind(section, add, edit, remove);
		Actions.bind(table, add, edit, remove);
	}

	private TableViewer createTable(Composite comp) {
		TableViewer table = Tables.createViewer(comp, "Komponente",
				"Investitionskosten","Nutzungsdauer", "Instandsetzung",
				"Wartung und Inspektion", "Aufwand für Bedienen");
		Tables.bindColumnWidths(table, 0.2, 0.16, 0.16, 0.16, 0.16, 0.16);
		return table;
	}
}
