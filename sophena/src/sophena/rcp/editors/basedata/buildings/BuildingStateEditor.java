package sophena.rcp.editors.basedata.buildings;

import java.util.List;
import java.util.UUID;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import sophena.db.daos.RootEntityDao;
import sophena.model.BuildingState;
import sophena.model.BuildingType;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;

public class BuildingStateEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("data.building.states",
				"Gebäudetypen");
		Editors.open(input, "sophena.BuildingStateEditor");
	}

	@Override
	protected void addPages() {
		try {
			addPage(new Page());
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}

	private class Page extends FormPage {

		private RootEntityDao<BuildingState> dao;
		private List<BuildingState> states;

		public Page() {
			super(BuildingStateEditor.this, "BuildingStatePage", "Gebäudetypen");
			dao = new RootEntityDao<>(BuildingState.class, App.getDb());
			states = dao.getAll();
		}

		@Override
		protected void createFormContent(IManagedForm managedForm) {
			ScrolledForm form = UI.formHeader(managedForm, "Gebäudetypen");
			FormToolkit toolkit = managedForm.getToolkit();
			Composite body = UI.formBody(form, toolkit);
			createSection(body, toolkit);
			form.reflow(true);
		}

		private void createSection(Composite parent, FormToolkit toolkit) {
			Section section = UI.section(parent, toolkit, "Gebäudetypen");
			UI.gridData(section, true, true);
			Composite comp = UI.sectionClient(section, toolkit);
			UI.gridLayout(comp, 1);
			TableViewer table = Tables.createViewer(comp, "Gebäudetyp",
					"Gebäudezustand", "Heizgrenztemperatur", "Warmwasseranteil",
					"Volllaststunden", "Voreinstellung");
			table.setLabelProvider(new TableLabel());
			table.setSorter(new TableSorter());
			table.setInput(states);
			double x = 1 / 6d;
			Tables.bindColumnWidths(table, x, x, x, x, x, x);
			bindActions(section, table);
		}

		private void bindActions(Section section, TableViewer table) {
			Action add = Actions.create(M.Add, Icon.ADD_16.des(),
					() -> add(table));
			Action edit = Actions.create(M.Edit, Icon.EDIT_16.des(),
					() -> edit(table));
			Action del = Actions.create(M.Delete, Icon.DELETE_16.des(),
					() -> delete(table));
			Actions.bind(section, add, edit, del);
			Actions.bind(table, add, edit, del);
			Tables.onDoubleClick(table, (e) -> edit(table));
		}

		private void add(TableViewer table) {
			BuildingState s = new BuildingState();
			s.id = UUID.randomUUID().toString();
			s.name = "Neuer Gebäudezustand";
			s.type = BuildingType.OTHER;
			s.heatingLimit = 15;
			s.waterFraction = 10;
			s.loadHours = 2500;
			s.index = 0;
			for (BuildingState state : states) {
				if (state.type != s.type)
					continue;
				s.index = Math.max(s.index, state.index + 1);
			}
			s.isDefault = s.index == 0;
			s.isProtected = false;
			if (StateWizard.open(s) != Window.OK)
				return;
			dao.insert(s);
			states.add(s);
			table.setInput(states);
		}

		private void edit(TableViewer table) {
			BuildingState s = Viewers.getFirstSelected(table);
			if (s == null || s.isProtected)
				return;
			if (StateWizard.open(s) != Window.OK)
				return;
			int idx = states.indexOf(s);
			s = dao.update(s);
			states.set(idx, s);
			table.setInput(states);
		}

		private void delete(TableViewer table) {
			BuildingState s = Viewers.getFirstSelected(table);
			if (s == null || s.isProtected)
				return;
			boolean doIt = MsgBox.ask("Wirklich löschen?",
					"Soll der ausgewählte Gebäudezustand wirklich gelöscht werden?");
			if (!doIt)
				return;
			// TODO: usage search !!!
			dao.delete(s);
			states.remove(s);
			table.setInput(states);
		}
	}
}
