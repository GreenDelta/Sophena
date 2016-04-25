package sophena.rcp.editors.basedata.products;

import sophena.model.ProductType;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class HeatRecoveryEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("heat.recovery.products",
				"Wärmerückgewinnung");
		Editors.open(input, "sophena.products.HeatRecoveryEditor");
	}

	@Override
	protected void addPages() {
		try {
			addPage(new Page(this));
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}
	
	private class Page extends FormPage {

		RootEntityDao<HeatRecovery> dao;
		List<HeatRecovery> recoveries;

		Page() {
			super(FlueGasCleaningEditor.this, "HeatRecoveryPage",
					"Wärmerückgewinnungsanlagen");
			dao = new RootEntityDao<>(HeatRecovery.class, App.getDb());
			recoveries = dao.getAll();
			Sorters.byName(recoveries);
		}
		
		@Override
		protected void createFormContent(IManagedForm managedForm) {
			ScrolledForm form = UI.formHeader(managedForm, "Wärmerückgewinnungsanlagen");
			FormToolkit tk = managedForm.getToolkit();
			Composite body = UI.formBody(form, tk);
			createSection(body, tk);
			form.reflow(true);
		}
		
		private void createSection(Composite body, FormToolkit toolkit) {
			Section section = UI.section(body, toolkit, "Wärmerückgewinnungsanlagen");
			UI.gridData(section, true, true);
			Composite comp = UI.sectionClient(section, toolkit);
			UI.gridLayout(comp, 1);
			TableViewer table = Tables.createViewer(comp, "Bezeichnung",
					"Therm. Leistung", "Link", "Preis", "Brennstoff");
			table.setLabelProvider(new Label());
			table.setInput(cleanings);
			double x = 1.0 / 5.0;
			Tables.bindColumnWidths(table, x, x, x, x, x);
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
			HeatRecovery r = new HeatRecovery();
			r.id = UUID.randomUUID().toString();
			r.type = ProductType.HEAT_RECOVERY;
			r.name = "Neue Wärmerückgewinnung";
			if (HeatRecoveryWizard.open(c) != Window.OK)
				return;
			dao.insert(r);
			recoveries.add(r);
			table.setInput(recoveries);
		}
		
		private void edit(TableViewer table) {
			HeatRecovery r = Viewers.getFirstSelected(table);
			if (r == null)
				return;
			if (HeatRecoveryWizard.open(c) != Window.OK)
				return;
			try {
				int idx = recoveries.indexOf(r);
				r = dao.update(r);
				recoveries.set(idx, r);
				table.setInput(recoveries);
			} catch (Exception e) {
				log.error("failed to update", e);
			}
		}
		
		private void delete(TableViewer table) {
			HeatRecovery r = Viewers.getFirstSelected(table);
			if (r == null)
				return;
			boolean doIt = MsgBox.ask("Wirklich löschen?",
					"Soll die ausgewählte Wärmerückgewinnung wirklich gelöscht werden?");
			if (!doIt)
				return;
			List<SearchResult> usage = new UsageSearch(App.getDb()).of(r);
			if (!usage.isEmpty()) {
				UsageError.show(usage);
				return;
			}
			try {
				dao.delete(r);
				recoveries.remove(r);
				table.setInput(recoveries);
			} catch (Exception e) {
				log.error("failed to delete " + r, e);
			}
		}		
	}
	
	private class Label extends BaseTableLabel {

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof HeatRecovery))
				return null;
			HeatRecovery hrc = (HeatRecovery) obj;
			switch (col) {
			case 0:
				return hrc.name;
			case 1:
				return Num.str(hrc.power) + " kW";
			case 2:
				return hrc.url;
			case 3:
				return hrc.purchasePrice == null ? null
						: Num.str(hrc.purchasePrice) + " EUR";
			case 4:
				return hrc.fuel != null ? hrc.fuel.name : null;
			default:
				return null;
			}
		}
	}
}
