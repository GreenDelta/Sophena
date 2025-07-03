package sophena.rcp.editors.biogas.substrate;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.Database;
import sophena.db.usage.SearchResult;
import sophena.db.usage.UsageSearch;
import sophena.model.BiogasSubstrate;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.editors.Editor;
import sophena.rcp.editors.basedata.UsageError;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;

public class SubstrateEditor extends Editor {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("data.biogas.substrates", "Biogas Substrate");
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
		private List<BiogasSubstrate> substrates;

		public Page() {
			super(SubstrateEditor.this, "SubstrateEditorPage", "Biogas Substrate");
			initData();
		}

		private void initData() {
			substrates = db.getAll(BiogasSubstrate.class);
			Sorters.sortBaseData(substrates);
		}

		@Override
		protected void createFormContent(IManagedForm managedForm) {
			ScrolledForm form = UI.formHeader(managedForm, "Biogas Substrate");
			FormToolkit toolkit = managedForm.getToolkit();
			Composite body = UI.formBody(form, toolkit);
			createSubstrateSection(body, toolkit);
			form.reflow(true);
		}

		private void createSubstrateSection(Composite body, FormToolkit toolkit) {
			Section section = UI.section(body, toolkit, "Biogas Substrate");
			UI.gridData(section, true, true);
			Composite comp = UI.sectionClient(section, toolkit);
			UI.gridLayout(comp, 1);
			TableViewer table = Tables.createViewer(comp,
					"Substrat", "TS [%]", "oTS [%]", "Biogasproduktion [m³/t oTS]",
					"Methangehalt [%]", "CO2 Emissionen [g/kWh]");
			table.setLabelProvider(new SubstrateTableLabel());
			table.setInput(substrates);
			double w = 1d / 6d;
			Tables.bindColumnWidths(table, w, w, w, w, w, w);
			bindActions(section, table);
		}

		private void bindActions(Section section, TableViewer table) {
			Action add = Actions.create(M.Add, Icon.ADD_16.des(),
					() -> addSubstrate(table));
			Action edit = Actions.create(M.Edit, Icon.EDIT_16.des(),
					() -> editSubstrate(table));
			Action copy = Actions.create(M.Copy, Icon.COPY_16.des(),
					() -> copySubstrate(table));
			Action del = Actions.create(M.Delete, Icon.DELETE_16.des(),
					() -> deleteSubstrate(table));
			Actions.bind(section, add, edit, copy, del);
			Actions.bind(table, add, edit, copy, del);
			Tables.onDoubleClick(table, (e) -> editSubstrate(table));
		}

		private void addSubstrate(TableViewer table) {
			BiogasSubstrate substrate = new BiogasSubstrate();
			substrate.id = UUID.randomUUID().toString();
			substrate.name = "Neues Substrat";
			substrate.dryMatter = 20.0;
			substrate.organicDryMatter = 80.0;
			substrate.biogasProduction = 400.0;
			substrate.methaneContent = 55.0;
			substrate.co2Emissions = 0.0;
			if (SubstrateWizard.open(substrate) != Window.OK)
				return;
			try {
				substrate = db.insert(substrate);
				substrates.add(substrate);
				table.setInput(substrates);
			} catch (Exception e) {
				log.error("failed to add substrate {}", substrate, e);
			}
		}

		private void editSubstrate(TableViewer table) {
			BiogasSubstrate substrate = Viewers.getFirstSelected(table);
			if (substrate == null)
				return;
			if (SubstrateWizard.open(substrate) != Window.OK)
				return;
			try {
				int idx = substrates.indexOf(substrate);
				substrate = db.update(substrate);
				substrates.set(idx, substrate);
				table.setInput(substrates);
			} catch (Exception e) {
				log.error("failed to update substrate {}", substrate, e);
			}
		}

		private void copySubstrate(TableViewer table) {
			BiogasSubstrate substrate = Viewers.getFirstSelected(table);
			if (substrate == null)
				return;
			BiogasSubstrate copy = substrate.copy();
			copy.id = UUID.randomUUID().toString();
			copy.isProtected = false;
			if (SubstrateWizard.open(copy) != Window.OK)
				return;
			db.insert(copy);
			substrates.add(copy);
			table.setInput(substrates);
		}

		private void deleteSubstrate(TableViewer table) {
			BiogasSubstrate substrate = Viewers.getFirstSelected(table);
			if (substrate == null || substrate.isProtected)
				return;
			boolean doIt = MsgBox.ask(M.Delete,
					"Soll das ausgewählte Substrat wirklich gelöscht werden?");
			if (!doIt)
				return;
			List<SearchResult> usage = new UsageSearch(App.getDb()).of(substrate);
			if (!usage.isEmpty()) {
				UsageError.show(usage);
				return;
			}
			try {
				db.delete(substrate);
				substrates.remove(substrate);
				table.setInput(substrates);
			} catch (Exception e) {
				log.error("failed to delete substrate {}", substrate, e);
			}
		}
	}
}
