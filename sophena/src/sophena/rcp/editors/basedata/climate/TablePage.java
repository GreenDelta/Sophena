package sophena.rcp.editors.basedata.climate;

import java.util.List;

import org.eclipse.jface.action.Action;
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

import sophena.db.daos.WeatherStationDao;
import sophena.model.WeatherStation;
import sophena.model.descriptors.WeatherStationDescriptor;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.Numbers;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;

class TablePage extends FormPage {

	private WeatherStationDao dao = new WeatherStationDao(App.getDb());

	public TablePage(ClimateDataEditor editor) {
		super(editor, "ClimateDataEditor.Page",
				M.ClimateData);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = UI.formHeader(managedForm, M.ClimateData);
		FormToolkit toolkit = managedForm.getToolkit();
		Composite body = UI.formBody(form, toolkit);
		Section section = UI.section(body, toolkit, M.ClimateData);
		UI.gridData(section, true, true);
		Composite comp = UI.sectionClient(section, toolkit);
		UI.gridLayout(comp, 1);
		TableViewer table = Tables.createViewer(comp, "Wetterstation",
				"Längengrad", "Breitengrad", "Höhe");
		table.setLabelProvider(new Label());
		List<WeatherStationDescriptor> list = dao.getDescriptors();
		Sorters.byName(list);
		table.setInput(list);
		Tables.bindColumnWidths(table, 0.25, 0.25, 0.25, 0.25);
		bindActions(section, table);
		form.reflow(true);
	}

	private void bindActions(Section section, TableViewer table) {
		Action open = Actions.create(
				"Temperaturverlauf",
				Images.OPEN_16.des(),
				() -> openClimateCurve(table));
		Actions.bind(section, open);
		Actions.bind(table, open);
	}

	private void openClimateCurve(TableViewer table) {
		WeatherStationDescriptor d = Viewers.getFirstSelected(table);
		if (d == null)
			return;
		WeatherStation station = dao.get(d.id);
		ClimateDataChart chart = new ClimateDataChart(UI.shell(), station);
		chart.open();
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return col == 0 ? Images.CLIMATE_16.img() : null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof WeatherStationDescriptor))
				return null;
			WeatherStationDescriptor d = (WeatherStationDescriptor) obj;
			switch (col) {
			case 0:
				return d.name;
			case 1:
				return Numbers.toString(d.longitude);
			case 2:
				return Numbers.toString(d.latitude);
			case 3:
				return Numbers.toString(d.altitude) + " m";
			default:
				return null;
			}
		}
	}

}
