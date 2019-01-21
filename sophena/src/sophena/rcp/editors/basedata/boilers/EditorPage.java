package sophena.rcp.editors.basedata.boilers;

import java.util.ArrayList;
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

import sophena.Labels;
import sophena.db.daos.BoilerDao;
import sophena.db.usage.SearchResult;
import sophena.db.usage.UsageSearch;
import sophena.model.Boiler;
import sophena.model.ProductType;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.editors.Editor;
import sophena.rcp.editors.basedata.BaseTableLabel;
import sophena.rcp.editors.basedata.ProductTables;
import sophena.rcp.editors.basedata.UsageError;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Num;

class EditorPage extends FormPage {

	private Logger log = LoggerFactory.getLogger(getClass());

	private BoilerDao dao = new BoilerDao(App.getDb());
	private List<Boiler> boilers;
	private final ProductType type;

	public EditorPage(Editor editor, ProductType type) {
		super(editor, "BoilerEditorPage", Labels.getPlural(type));
		this.type = type;
		boilers = new ArrayList<>(dao.getAll(type));
		Sorters.boilers(boilers);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = UI.formHeader(managedForm,
				Labels.getPlural(type));
		FormToolkit toolkit = managedForm.getToolkit();
		Composite body = UI.formBody(form, toolkit);
		createBoilerSection(body, toolkit);
		form.reflow(true);
	}

	private void createBoilerSection(Composite parent, FormToolkit toolkit) {
		Section section = UI.section(parent, toolkit,
				Labels.getPlural(type));
		UI.gridData(section, true, true);
		Composite comp = UI.sectionClient(section, toolkit);
		UI.gridLayout(comp, 1);
		TableViewer table = Tables.createViewer(comp, getColumns());
		table.setLabelProvider(new BoilerLabel());
		table.setInput(boilers);
		if (type == ProductType.COGENERATION_PLANT) {
			double x = 1 / 7.0;
			Tables.bindColumnWidths(table, x, x, x, x, x, x, x);
		} else {
			double x = 1 / 5.0;
			Tables.bindColumnWidths(table, x, x, x, x, x);
		}
		bindBoilerActions(section, table);
	}

	private String[] getColumns() {
		String erLabel = type == ProductType.HEAT_PUMP
				? "COP"
				: "Wirkungsgrad";
		if (type == ProductType.COGENERATION_PLANT)
			return new String[] { "Produktgruppe", "Bezeichnung", "Hersteller",
					"Max. Leistung el.", "Wirkungsgrad el.",
					"Max. Leistung th.",
					"Wirkungsgrad th." };
		else
			return new String[] { "Produktgruppe", "Bezeichnung", "Hersteller",
					"Maximale Leistung", erLabel };
	}

	private void bindBoilerActions(Section section, TableViewer table) {
		Action add = Actions.create(M.Add, Icon.ADD_16.des(),
				() -> addBoiler(table));
		Action edit = Actions.create(M.Edit, Icon.EDIT_16.des(),
				() -> editBoiler(table));
		Action saveAs = Actions.create(M.Copy, Icon.COPY_16.des(),
				() -> saveAs(table));
		Action del = Actions.create(M.Delete, Icon.DELETE_16.des(),
				() -> deleteBoiler(table));
		Actions.bind(section, add, edit, saveAs, del);
		Actions.bind(table, add, edit, saveAs, del);
		Tables.onDoubleClick(table, e -> editBoiler(table));
	}

	private void addBoiler(TableViewer table) {
		Boiler boiler = new Boiler();
		boiler.isCoGenPlant = type == ProductType.COGENERATION_PLANT;
		boiler.type = type;
		boiler.id = UUID.randomUUID().toString();
		boiler.name = Labels.get(type) + " - neu";
		boiler.efficiencyRate = type == ProductType.HEAT_PUMP ? 1.0 : 0.8;
		if (BoilerWizard.open(boiler) != Window.OK)
			return;
		dao.insert(boiler);
		boilers.add(boiler);
		table.setInput(boilers);
	}

	private void editBoiler(TableViewer table) {
		Boiler boiler = Viewers.getFirstSelected(table);
		if (boiler == null)
			return;
		if (BoilerWizard.open(boiler) != Window.OK)
			return;
		try {
			int idx = boilers.indexOf(boiler);
			boiler = dao.update(boiler);
			boilers.set(idx, boiler);
			table.setInput(boilers);
		} catch (Exception e) {
			log.error("failed to update boiler ", boiler, e);
		}
	}

	private void saveAs(TableViewer table) {
		Boiler b = Viewers.getFirstSelected(table);
		if (b == null)
			return;
		Boiler copy = b.clone();
		copy.id = UUID.randomUUID().toString();
		copy.isProtected = false;
		if (BoilerWizard.open(copy) != Window.OK)
			return;
		dao.insert(copy);
		boilers.add(copy);
		table.setInput(boilers);
	}

	private void deleteBoiler(TableViewer table) {
		Boiler boiler = Viewers.getFirstSelected(table);
		if (boiler == null || boiler.isProtected)
			return;
		boolean doIt = MsgBox.ask(M.Delete,
				"Soll das ausgewählte Produkt wirklich gelöscht werden?");
		if (!doIt)
			return;
		List<SearchResult> usage = new UsageSearch(App.getDb()).of(boiler);
		if (!usage.isEmpty()) {
			UsageError.show(usage);
			return;
		}
		try {
			dao.delete(boiler);
			boilers.remove(boiler);
			table.setInput(boilers);
		} catch (Exception e) {
			log.error("failed to delete boiler " + boiler, e);
		}
	}

	private class BoilerLabel extends BaseTableLabel {

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof Boiler))
				return null;
			Boiler boiler = (Boiler) obj;
			if (col < 3)
				return ProductTables.getText(boiler, col);
			boolean coGen = type == ProductType.COGENERATION_PLANT;
			switch (col) {
			case 3:
				return coGen ? s(boiler.maxPowerElectric, "kW")
						: s(boiler.maxPower, "kW");
			case 4:
				if (coGen)
					return s(boiler.efficiencyRateElectric * 100d, "%");
				if (type == ProductType.HEAT_PUMP)
					return Num.str(boiler.efficiencyRate);
				return s(boiler.efficiencyRate * 100d, "%");
			case 5:
				return coGen ? s(boiler.maxPower, "kW") : null;
			case 6:
				return coGen ? s(boiler.efficiencyRate * 100d, "%") : null;
			default:
				return null;
			}
		}

		private String s(double val, String unit) {
			return Num.str(val) + " " + unit;
		}

	}
}