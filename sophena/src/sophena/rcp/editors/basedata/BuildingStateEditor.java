package sophena.rcp.editors.basedata;

import java.util.List;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import sophena.db.daos.RootEntityDao;
import sophena.model.Boiler;
import sophena.model.BuildingState;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.Labels;
import sophena.rcp.Numbers;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;

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
			super(BuildingStateEditor.this, "BuildingStatePage",
					"Gebäudetypen");
			dao = new RootEntityDao<>(BuildingState.class, App.getDb());
			states = dao.getAll();
		}

		@Override
		protected void createFormContent(IManagedForm managedForm) {
			ScrolledForm form = UI.formHeader(managedForm, "Gebäudetypen");
			FormToolkit toolkit = managedForm.getToolkit();
			Composite body = UI.formBody(form, toolkit);
			// TODO: create page content
			form.reflow(true);
		}
		
		private void createStateSection(Composite parent,
				FormToolkit toolkit) {
			Section section = UI.section(parent, toolkit, "Gebäudetypen");
			UI.gridData(section, true, true);
			Composite comp = UI.sectionClient(section, toolkit);
			UI.gridLayout(comp, 1);
			TableViewer table = Tables.createViewer(comp, "Bezeichnung",
					"Link", "Preis", "Leistungsbereich", "Brennstoff");
			//table.setLabelProvider(new BoilerLabel());
			table.setInput(states);
			Tables.bindColumnWidths(table, 0.2, 0.2, 0.2, 0.2, 0.2);
			//bindBoilerActions(section, table);
		}
		
		private class BoilerLabel extends LabelProvider
		implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int col) {
		return col == 0 ? Images.BOILER_16.img() : null;
	}

	@Override
	public String getColumnText(Object element, int col) {
		if (!(element instanceof BuildingState))
			return null;
		BuildingState boiler = (BuildingState) element;
		switch (col) {
		case 0:
			return boiler.name;
		case 1:
			return boiler.url;
		case 2:
			return getPrice(boiler);
		case 3:
			return getPowerInfo(boiler);
		case 4:
			return getFuelLabel(boiler);
		default:
			return null;
		}
	}

	private String getPrice(BuildingState boiler) {
		if (boiler == null || boiler.purchasePrice == null)
			return null;
		else
			return Numbers.toString(boiler.purchasePrice) + " EUR";
	}

	private String getPowerInfo(BuildingState boiler) {
		if (boiler == null)
			return null;
		String min = Numbers.toString(boiler.minPower);
		String max = Numbers.toString(boiler.maxPower);
		String eta = Numbers.toString(boiler.efficiencyRate);
		return min + " kW - " + max + " kW (\u03B7=" + eta + "%)";
	}

	private String getFuelLabel(BuildingState boiler) {
		if (boiler.fuel != null)
			return boiler.fuel.name;
		else
			return Labels.get(boiler.woodAmountType);
	}
}
		
	}
}
