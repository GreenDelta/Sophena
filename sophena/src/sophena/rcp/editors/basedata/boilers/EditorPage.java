package sophena.rcp.editors.basedata.boilers;

import java.util.List;
import java.util.UUID;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.BoilerDao;
import sophena.db.usage.SearchResult;
import sophena.db.usage.UsageSearch;
import sophena.model.Boiler;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.Labels;
import sophena.rcp.M;
import sophena.rcp.editors.Editor;
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
	private boolean isForCoGen;

	public EditorPage(Editor editor, boolean isForCoGen) {
		super(editor, "BoilerEditorPage",
				isForCoGen ? "KWK-Anlage" : "Heizkessel");
		this.isForCoGen = isForCoGen;
		boilers = isForCoGen ? dao.getCoGenPlants() : dao.getBoilers();
		Sorters.byName(boilers);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = UI.formHeader(managedForm,
				isForCoGen ? "KWK-Anlage" : "Heizkessel");
		FormToolkit toolkit = managedForm.getToolkit();
		Composite body = UI.formBody(form, toolkit);
		createBoilerSection(body, toolkit);
		form.reflow(true);
	}

	private void createBoilerSection(Composite parent, FormToolkit toolkit) {
		Section section = UI.section(parent, toolkit,
				isForCoGen ? "KWK-Anlage" : "Heizkessel");
		UI.gridData(section, true, true);
		Composite comp = UI.sectionClient(section, toolkit);
		UI.gridLayout(comp, 1);
		TableViewer table = Tables.createViewer(comp, getColumns());
		table.setLabelProvider(new BoilerLabel());
		table.setInput(boilers);
		double x = 1 / (isForCoGen ? 7.0 : 6.0);
		Tables.bindColumnWidths(table, x, x, x, x, x, (isForCoGen ? x : 0));
		bindBoilerActions(section, table);
	}

	private String[] getColumns() {
		if (isForCoGen)
			return new String[] { "Produktgruppe", "Bezeichnung", "Hersteller",
					"Max. Leistung el.", "Wirkungsgrad el.", "Max. Leistung th.",
					"Wirkungsgrad th." };
		else
			return new String[] { "Produktgruppe", "Bezeichnung", "Hersteller",
					"Maximale Leistung", "Wirkungsgrad" };
	}

	private void bindBoilerActions(Section section, TableViewer table) {
		Action add = Actions.create(M.Add, Icon.ADD_16.des(),
				() -> addBoiler(table));
		Action edit = Actions.create(M.Edit, Icon.EDIT_16.des(),
				() -> editBoiler(table));
		Action del = Actions.create(M.Delete, Icon.DELETE_16.des(),
				() -> deleteBoiler(table));
		Actions.bind(section, add, edit, del);
		Actions.bind(table, add, edit, del);
		Tables.onDoubleClick(table, (e) -> editBoiler(table));
	}

	private void addBoiler(TableViewer table) {
		Boiler boiler = new Boiler();
		boiler.isCoGenPlant = isForCoGen;
		boiler.id = UUID.randomUUID().toString();
		boiler.name = isForCoGen ? "Neue KWK-Anlage" : "Neuer Heizkessel";
		boiler.efficiencyRate = 80;
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

	private void deleteBoiler(TableViewer table) {
		Boiler boiler = Viewers.getFirstSelected(table);
		if (boiler == null)
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

	private class BoilerLabel extends LabelProvider
			implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int col) {
			if (col != 0)
				return null;
			return isForCoGen ? Icon.CO_GEN_16.img() : Icon.BOILER_16.img();
		}

		@Override
		public String getColumnText(Object element, int col) {
			if (!(element instanceof Boiler))
				return null;
			Boiler boiler = (Boiler) element;
			switch (col) {
			case 0:
				return boiler.group != null ? boiler.group.name : null;
			case 1:
				return boiler.name;
			case 2:
				return boiler.manufacturer != null ? boiler.manufacturer.name : null;
			case 3:
				if (isForCoGen) {
					return Num.str(boiler.maxPowerElectric) + " kW";
				} else {
					return Num.str(boiler.maxPower) + " kW";
				}
			case 4:
				if (isForCoGen) {
					return Num.str(boiler.efficiencyRateElectric) + " %";
				} else {
					return Num.str(boiler.efficiencyRate) + " %";
				}
			case 5:
				if (isForCoGen) {
					return Num.str(boiler.maxPower) + " kW";
				}
			case 6:
				if (isForCoGen) {
					return Num.str(boiler.efficiencyRate) + "%";
				}
			default:
				return null;
			}
		}

		private String getPrice(Boiler boiler) {
			if (boiler == null || boiler.purchasePrice == null)
				return null;
			else
				return Num.str(boiler.purchasePrice) + " EUR";
		}

		private String getPowerInfo(Boiler boiler) {
			if (boiler == null)
				return null;
			String min = Num.str(boiler.minPower);
			String max = Num.str(boiler.maxPower);
			String eta = Num.str(boiler.efficiencyRate);
			return min + " kW - " + max + " kW (\u03B7=" + eta + "%)";
		}

		private String getElectricPowerInfo(Boiler boiler) {
			if (boiler == null)
				return null;
			String min = Num.str(boiler.minPowerElectric);
			String max = Num.str(boiler.maxPowerElectric);
			String eta = Num.str(boiler.efficiencyRateElectric);
			return min + " kW - " + max + " kW (\u03B7=" + eta + "%)";
		}

		private String getFuelLabel(Boiler boiler) {
			if (boiler.fuel != null)
				return boiler.fuel.name;
			else
				return Labels.get(boiler.woodAmountType);
		}
	}
}