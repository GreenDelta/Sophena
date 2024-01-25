package sophena.rcp.editors.basedata.solarcollectors;

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
import sophena.db.usage.SearchResult;
import sophena.db.usage.UsageSearch;
import sophena.model.ProductType;
import sophena.model.SolarCollector;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.editors.Editor;
import sophena.rcp.editors.basedata.BaseTableLabel;
import sophena.rcp.editors.basedata.ProductTables;
import sophena.rcp.editors.basedata.UsageError;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Num;

public class SolarCollectorEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("data.solarCollectors", M.SolarThermalPlant);
		Editors.open(input, "sophena.SolarCollectorEditor");
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

		private RootEntityDao<SolarCollector> dao;
		private List<SolarCollector> solarCollectors;

		public Page() {
			super(SolarCollectorEditor.this, "SolarCollectorEditorPage", M.SolarThermalPlant);
			dao = new RootEntityDao<>(SolarCollector.class, App.getDb());
			solarCollectors = dao.getAll();
			Sorters.solarCollectors(solarCollectors);
		}

		@Override
		protected void createFormContent(IManagedForm managedForm) {
			ScrolledForm form = UI.formHeader(managedForm, M.SolarThermalPlant);
			FormToolkit toolkit = managedForm.getToolkit();
			Composite body = UI.formBody(form, toolkit);
			createSection(body, toolkit);
			form.reflow(true);
		}

		private void createSection(Composite body, FormToolkit toolkit) {
			Section section = UI.section(body, toolkit, M.SolarThermalPlant);
			UI.gridData(section, true, true);
			Composite comp = UI.sectionClient(section, toolkit);
			UI.gridLayout(comp, 1);
			TableViewer table = Tables.createViewer(comp, "Produktgruppe", "Bezeichnung", "Hersteller", M.CollectorArea);
			table.setLabelProvider(new Label());
			table.setInput(solarCollectors);
			double x = 1.0 / 6.0;
			Tables.bindColumnWidths(table, x, x, x, x);
			bindActions(section, table);
		}

		private void bindActions(Section section, TableViewer table) {
			Action add = Actions.create(M.Add, Icon.ADD_16.des(),
					() -> add(table));
			Action edit = Actions.create(M.Edit, Icon.EDIT_16.des(),
					() -> edit(table));
			Action saveAs = Actions.create(M.Copy, Icon.COPY_16.des(),
					() -> saveAs(table));
			Action del = Actions.create(M.Delete, Icon.DELETE_16.des(),
					() -> delete(table));
			Actions.bind(section, add, edit, saveAs, del);
			Actions.bind(table, add, edit, saveAs, del);
			Tables.onDoubleClick(table, (e) -> edit(table));
		}

		private void add(TableViewer table) {
			SolarCollector s = new SolarCollector();
			s.type = ProductType.SOLAR_THERMAL_PLANT;
			s.id = UUID.randomUUID().toString();
			s.name = "Neue Solarthermische Anlage";
			if (SolarCollectorWizard.open(s) != Window.OK)
				return;
			dao.insert(s);
			solarCollectors.add(s);
			table.setInput(solarCollectors);
		}

		private void edit(TableViewer table) {
			SolarCollector s = Viewers.getFirstSelected(table);
			if (s == null)
				return;
			if (SolarCollectorWizard.open(s) != Window.OK)
				return;
			try {
				int idx = solarCollectors.indexOf(s);
				s = dao.update(s);
				solarCollectors.set(idx, s);
				table.setInput(solarCollectors);
			} catch (Exception e) {
				log.error("failed to update solar collector");
			}
		}

		private void saveAs(TableViewer table) {
			SolarCollector s = Viewers.getFirstSelected(table);
			if (s == null)
				return;
			SolarCollector copy = s.copy();
			copy.id = UUID.randomUUID().toString();
			copy.isProtected = false;
			if (SolarCollectorWizard.open(copy) != Window.OK)
				return;
			dao.insert(copy);
			solarCollectors.add(copy);
			table.setInput(solarCollectors);
		}

		private void delete(TableViewer table) {
			SolarCollector s = Viewers.getFirstSelected(table);
			if (s == null || s.isProtected)
				return;
			boolean doIt = MsgBox.ask("Wirklich löschen?",
					"Soll die ausgewählte Wärmeleitung wirklich gelöscht werden?");
			if (!doIt)
				return;
			List<SearchResult> usage = new UsageSearch(App.getDb()).of(s);
			if (!usage.isEmpty()) {
				UsageError.show(usage);
				return;
			}
			try {
				dao.delete(s);
				solarCollectors.remove(s);
				table.setInput(solarCollectors);
			} catch (Exception e) {
				log.error("failed to delete solar collector " + s, e);
			}
		}

	}

	private class Label extends BaseTableLabel {

		@Override
		public String getColumnText(Object e, int col) {
			if (!(e instanceof SolarCollector))
				return null;
			SolarCollector s = (SolarCollector) e;
			if (col < 3)
				return ProductTables.getText(s, col);
			switch (col) {
			case 3:
				return Num.str(s.collectorArea);
			default:
				return null;
			}
		}
	}
}