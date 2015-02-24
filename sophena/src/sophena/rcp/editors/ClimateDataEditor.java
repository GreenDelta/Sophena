package sophena.rcp.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.wizards.ClimateDataImportWizard;

public class ClimateDataEditor extends FormEditor {

	private Logger log = LoggerFactory.getLogger(getClass());

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("climate.data", M.ClimateData);
		Editors.open(input, "sophena.ClimateDataEditor");
	}


	@Override
	protected void addPages() {
		try {
			addPage(new Page());
		} catch (Exception e) {
			log.error("failed to add page", e);
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

	private class Page extends FormPage {

		public Page() {
			super(ClimateDataEditor.this, "ClimateDataEditor.Page", M.ClimateData);
		}

		@Override
		protected void createFormContent(IManagedForm managedForm) {
			ScrolledForm form = UI.formHeader(managedForm, M.ClimateData);
			FormToolkit toolkit = managedForm.getToolkit();
			Composite body = UI.formBody(form, toolkit);
			Section section = UI.section(body, toolkit, M.ClimateData);
			UI.gridData(section, true, true);
			Composite comp = UI.sectionClient(section, toolkit);
			UI.gridLayout(comp, 1);
			TableViewer table = Tables.createViewer(comp, "#Wetterstation",
					"#Längengrad", "#Breitengrad", "#Normaußentemperatur");
			Tables.bindColumnWidths(table, 0.25, 0.25, 0.25, 0.25);
			bindActions(section, table);
			form.reflow(true);
		}

		private void bindActions(Section section, TableViewer table) {
			Action input = Actions.create("#Importieren", Images.IMPORT_16.des(),
					ClimateDataImportWizard::open);
			Actions.bind(section, input);
			Actions.bind(table, input);
		}
	}
}
