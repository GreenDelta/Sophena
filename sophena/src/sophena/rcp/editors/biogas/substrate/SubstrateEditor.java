package sophena.rcp.editors.biogas.substrate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.Database;
import sophena.db.usage.UsageSearch;
import sophena.model.biogas.Substrate;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.editors.Editor;
import sophena.rcp.editors.basedata.BaseTableLabel;
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

public class SubstrateEditor extends Editor {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public static void open() {
		var input = new KeyEditorInput("data.biogas.substrates", "Biogassubstrate");
		Editors.open(input, "sophena.SubstrateEditor");
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

		private final Database db = App.getDb();
		private final List<Substrate> substrates;

		Page() {
			super(SubstrateEditor.this, "SubstrateEditorPage", "Biogassubstrate");
			substrates = new ArrayList<>( db.getAll(Substrate.class));
			Sorters.sortBaseData(substrates);
		}

		@Override
		protected void createFormContent(IManagedForm mForm) {
			var form = UI.formHeader(mForm, "Biogassubstrate");
			var tk = mForm.getToolkit();
			var body = UI.formBody(form, tk);
			createSection(body, tk);
			form.reflow(true);
		}

		private void createSection(Composite body, FormToolkit tk) {
			var section = UI.section(body, tk, "Biogassubstrate");
			UI.gridData(section, true, true);
			var comp = UI.sectionClient(section, tk);
			UI.gridLayout(comp, 1);
			var table = Tables.createViewer(comp,
					"Substrat",
					"TS [%]",
					"oTS [%]",
					"Biogasproduktion [m³/t oTS]",
					"Methangehalt [%]",
					"CO2 Emissionen [g/kWh]"
			);
			table.setLabelProvider(new TableLabel());
			table.setInput(substrates);
			double w = 1d / 6d;
			Tables.bindColumnWidths(table, w, w, w, w, w, w);
			bindActions(section, table);
		}

		private void bindActions(Section section, TableViewer table) {
			var add = Actions.create(M.Add, Icon.ADD_16.des(),
					() -> addSubstrate(table));
			var edit = Actions.create(M.Edit, Icon.EDIT_16.des(),
					() -> editSubstrate(table));
			var copy = Actions.create(M.Copy, Icon.COPY_16.des(),
					() -> copySubstrate(table));
			var del = Actions.create(M.Delete, Icon.DELETE_16.des(),
					() -> deleteSubstrate(table));
			Actions.bind(section, add, edit, copy, del);
			Actions.bind(table, add, edit, copy, del);
			Tables.onDoubleClick(table, (e) -> editSubstrate(table));
		}

		private void addSubstrate(TableViewer table) {
			var s = new Substrate();
			s.id = UUID.randomUUID().toString();
			s.name = "Neues Substrat";
			s.dryMatter = 20.0;
			s.organicDryMatter = 80.0;
			s.biogasProduction = 400.0;
			s.methaneContent = 55.0;
			s.co2Emissions = 0.0;
			if (SubstrateWizard.open(s) != Window.OK)
				return;
			try {
				s = db.insert(s);
				substrates.add(s);
				table.setInput(substrates);
			} catch (Exception e) {
				log.error("failed to add substrate {}", s, e);
			}
		}

		private void editSubstrate(TableViewer table) {
			Substrate s = Viewers.getFirstSelected(table);
			if (s == null)
				return;
			if (SubstrateWizard.open(s) != Window.OK)
				return;
			try {
				int idx = substrates.indexOf(s);
				s = db.update(s);
				substrates.set(idx, s);
				table.setInput(substrates);
			} catch (Exception e) {
				log.error("failed to update substrate {}", s, e);
			}
		}

		private void copySubstrate(TableViewer table) {
			Substrate s = Viewers.getFirstSelected(table);
			if (s == null)
				return;
			var copy = s.copy();
			copy.id = UUID.randomUUID().toString();
			copy.isProtected = false;
			if (SubstrateWizard.open(copy) != Window.OK)
				return;
			db.insert(copy);
			substrates.add(copy);
			table.setInput(substrates);
		}

		private void deleteSubstrate(TableViewer table) {
			Substrate s = Viewers.getFirstSelected(table);
			if (s == null || s.isProtected)
				return;
			boolean doIt = MsgBox.ask(M.Delete,
					"Soll das ausgewählte Substrat wirklich gelöscht werden?");
			if (!doIt)
				return;
			var usage = new UsageSearch(App.getDb()).of(s);
			if (!usage.isEmpty()) {
				UsageError.show(usage);
				return;
			}
			try {
				db.delete(s);
				substrates.remove(s);
				table.setInput(substrates);
			} catch (Exception e) {
				log.error("failed to delete substrate {}", s, e);
			}
		}
	}

	static class TableLabel extends BaseTableLabel {

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof Substrate s))
				return null;
			return switch (col) {
				case 0 -> s.name;
				case 1 -> Num.str(s.dryMatter);
				case 2 -> Num.str(s.organicDryMatter);
				case 3 -> Num.str(s.biogasProduction);
				case 4 -> Num.str(s.methaneContent);
				case 5 -> Num.str(s.co2Emissions);
				default -> null;
			};
		}
	}
}
