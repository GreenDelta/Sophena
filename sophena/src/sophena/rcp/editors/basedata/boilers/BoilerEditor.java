package sophena.rcp.editors.basedata.boilers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sophena.db.daos.BoilerDao;
import sophena.model.Boiler;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;

public class BoilerEditor extends FormEditor {

	private Logger log = LoggerFactory.getLogger(getClass());

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("data.boilers", "Heizkessel");
		Editors.open(input, "sophena.BoilerEditor");
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

		private BoilerDao dao = new BoilerDao(App.getDb());
		private List<Boiler> boilers = new ArrayList<>();

		public Page() {
			super(BoilerEditor.this, "BoilerEditorPage", "Heizkessel");
			boilers = dao.getAll();
			Collections.sort(boilers,
					(b1, b2) -> Strings.compare(b1.getName(), b2.getName()));
		}


		@Override
		protected void createFormContent(IManagedForm managedForm) {
			ScrolledForm form = UI.formHeader(managedForm, "Heizkessel");
			FormToolkit toolkit = managedForm.getToolkit();
			Composite body = UI.formBody(form, toolkit);
			createBoilerSection(body, toolkit);
			form.reflow(true);
		}

		private void createBoilerSection(Composite parent, FormToolkit toolkit) {
			Section section = UI.section(parent, toolkit, "Heizkessel");
			UI.gridData(section, true, true);
			Composite comp = UI.sectionClient(section, toolkit);
			UI.gridLayout(comp, 1);
			TableViewer table = Tables.createViewer(comp, "Bezeichnung",
					"Link", "Preis", "Leistungsbereich", "Brennstoff");
			table.setLabelProvider(new BoilerLabel());
			table.setInput(boilers);
			Tables.bindColumnWidths(table, 0.2, 0.2, 0.2, 0.2, 0.2);
			bindBoilerActions(section, table);
		}

		private void bindBoilerActions(Section section, TableViewer table) {
			Action add = Actions.create(M.Add, Images.ADD_16.des(),
					() -> addBoiler(table));
			Action edit = Actions.create(M.Edit, Images.EDIT_16.des(),
					() -> editBoiler(table));
			Action del = Actions.create(M.Delete, Images.DELETE_16.des(),
					() -> deleteBoiler(table));
			Actions.bind(section, add, edit, del);
			Actions.bind(table, add, edit, del);
		}

		private void addBoiler(TableViewer table) {
			Boiler boiler = new Boiler();
			boiler.setId(UUID.randomUUID().toString());
			boiler.setName("Neuer Heizkessel");
			boiler.setEfficiencyRate(80);
			if(BoilerWizard.open(boiler) != Window.OK)
				return;
			dao.insert(boiler);
			boilers.add(boiler);
			table.setInput(boilers);
		}

		private void editBoiler(TableViewer table) {
			Boiler boiler = Viewers.getFirstSelected(table);
			if (boiler == null)
				return;
			if(BoilerWizard.open(boiler) != Window.OK)
				return;
			try {
				int idx = boilers.indexOf(boiler);
				boiler = dao.update(boiler);
				boilers.set(idx, boiler);
				table.setInput(boilers);
			} catch(Exception e) {
				log.error("failed to update boiler ", boiler, e);
			}
		}

		private void deleteBoiler(TableViewer table) {
			Boiler boiler = Viewers.getFirstSelected(table);
			if (boiler == null)
				return;
			boolean doIt = MsgBox.ask(M.Delete,
					"Soll der ausgewählte Heizkessel wirklich gelöscht werden?");
			if (!doIt)
				return;
			try {
				dao.delete(boiler);
				boilers.remove(boiler);
				table.setInput(boilers);
			} catch (Exception e) {
				log.error("failed to delete boiler " + boiler, e);
			}
		}

		private class BoilerLabel extends LabelProvider
				implements ITableLabelProvider {

			@Override
			public Image getColumnImage(Object element, int col) {
				return col == 0 ? Images.BOILER_16.img() : null;
			}

			@Override
			public String getColumnText(Object element, int col) {
				if (!(element instanceof Boiler))
					return null;
				Boiler boiler = (Boiler) element;
				switch (col) {
					case 0:
						return boiler.getName();
					default:
						return null;
				}
			}
		}

	}


}
