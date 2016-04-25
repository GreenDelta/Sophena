package sophena.rcp.editors.basedata.products;

import sophena.model.ProductType;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class FlueGasCleaningEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("flue.gas.cleaning.products",
				"Rauchgasreinigung");
		Editors.open(input, "sophena.products.FlueGasCleaningEditor");
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

		RootEntityDao<FlueGasCleaning> dao;
		List<FlueGasCleaning> cleanings;

		Page() {
			super(FlueGasCleaningEditor.this, "FlueGasCleaningPage",
					"Rauchgasreinigungsanlagen");
			dao = new RootEntityDao<>(FlueGasCleaning.class, App.getDb());
			cleanings = dao.getAll();
			Sorters.byName(cleanings);
		}
		
		@Override
		protected void createFormContent(IManagedForm managedForm) {
			ScrolledForm form = UI.formHeader(managedForm, "Rauchgasreinigungsanlagen");
			FormToolkit tk = managedForm.getToolkit();
			Composite body = UI.formBody(form, tk);
			createSection(body, tk);
			form.reflow(true);
		}
		
		private void createSection(Composite body, FormToolkit toolkit) {
			Section section = UI.section(body, toolkit, "Rauchgasreinigungsanlagen");
			UI.gridData(section, true, true);
			Composite comp = UI.sectionClient(section, toolkit);
			UI.gridLayout(comp, 1);
			TableViewer table = Tables.createViewer(comp, "Bezeichnung",
					"Art", "Link", "Preis", "Max. Volumenstrom");
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
			FlueGasCleaning c = new FlueGasCleaning();
			c.id = UUID.randomUUID().toString();
			c.type = ProductType.FLUE_GAS_CLEANING;
			c.name = "Neue Rauchgasreinigung";
			if (FlueGasCleaningWizard.open(c) != Window.OK)
				return;
			dao.insert(c);
			cleanings.add(c);
			table.setInput(cleanings);
		}

		private void edit(TableViewer table) {
			FlueGasCleaning c = Viewers.getFirstSelected(table);
			if (c == null)
				return;
			if (FlueGasCleaningWizard.open(c) != Window.OK)
				return;
			try {
				int idx = cleanings.indexOf(c);
				c = dao.update(c);
				cleanings.set(idx, c);
				table.setInput(cleanings);
			} catch (Exception e) {
				log.error("failed to update flue gas cleaning", e);
			}
		}

		private void delete(TableViewer table) {
			FlueGasCleaning c = Viewers.getFirstSelected(table);
			if (c == null || c.isProtected)
				return;
			boolean doIt = MsgBox.ask("Wirklich löschen?",
					"Soll die ausgewählte Rauchgasreinigung wirklich gelöscht werden?");
			if (!doIt)
				return;
			List<SearchResult> usage = new UsageSearch(App.getDb()).of(c);
			if (!usage.isEmpty()) {
				UsageError.show(usage);
				return;
			}
			try {
				dao.delete(c);
				cleanings.remove(c);
				table.setInput(cleanings);
			} catch (Exception e) {
				log.error("failed to delete flue gas cleaning " + c, e);
			}
		}		
	}
	
	private class Label extends BaseTableLabel {

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof FlueGasCleaning))
				return null;
			FlueGasCleaning c = (FlueGasCleaning) obj;
			switch (col) {
			case 0:
				return c.name;
			case 1:
				return c.cleaningMethod;
			case 2:
				return c.url;
			case 3:
				return c.purchasePrice == null ? null
						: Num.str(c.purchasePrice) + " EUR";
			case 4:
				return Num.str(c.maxVolumeFlow) + " m3/h";
			default:
				return null;
			}
		}
	}
}