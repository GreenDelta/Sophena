package sophena.rcp.editors.basedata.climate;

import java.io.File;
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

import sophena.Labels;
import sophena.db.daos.WeatherStationDao;
import sophena.io.HoursProfile;
import sophena.model.Boiler;
import sophena.model.ProductType;
import sophena.model.WeatherStation;
import sophena.model.descriptors.WeatherStationDescriptor;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Num;

class TablePage extends FormPage {

	private WeatherStationDao dao = new WeatherStationDao(App.getDb());
	private List<WeatherStationDescriptor> weatherStationsList; 

	public TablePage(ClimateDataEditor editor) {
		super(editor, "ClimateDataEditor.Page",
				M.ClimateData);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = UI.formHeader(managedForm, M.ClimateData);
		FormToolkit toolkit = managedForm.getToolkit();
		Composite body = UI.formBody(form, toolkit);
		Section section = UI.section(body, toolkit, "Wetterstationen (" + M.DwdSourceInfo + ")");
		UI.gridData(section, true, true);
		Composite comp = UI.sectionClient(section, toolkit);
		UI.gridLayout(comp, 1);
		TableViewer table = Tables.createViewer(comp, "Wetterstation",
				"Längengrad", "Breitengrad", "Höhe");
		table.setLabelProvider(new Label());
		weatherStationsList = dao.getDescriptors();
		Sorters.byName(weatherStationsList);
		table.setInput(weatherStationsList);
		Tables.bindColumnWidths(table, 0.25, 0.25, 0.25, 0.25);
		bindActions(section, table);
		form.reflow(true);
	}

	private void bindActions(Section section, TableViewer table) {
		Action add = Actions.create(M.Add, Icon.ADD_16.des(),
				() -> addWeatherStation(table));
		Action edit = Actions.create(M.Edit, Icon.EDIT_16.des(),
				() -> editWeatherStation(table));
		Action del = Actions.create(M.Delete, Icon.DELETE_16.des(),
				() -> deleteWeatherStation(table));
		Action open = Actions.create("Temperaturverlauf anzeigen",
				Icon.OPEN_16.des(), () -> openClimateCurve(table));
		Action export = Actions.create("Temperaturverlauf exportieren",
				Icon.EXPORT_FILE_16.des(), () -> exportClimateCurve(table));
		Actions.bind(section, add, open, export);
		Actions.bind(table, add, open, export);
		Tables.onDoubleClick(table, e -> openClimateCurve(table));
	}

	private void addWeatherStation(TableViewer table) {
		WeatherStation weatherStation = new WeatherStation();
		weatherStation.id = UUID.randomUUID().toString();
		weatherStation.name = M.NewStation;
		if (ImportWizard.open(weatherStation) != Window.OK)
			return;
		dao.insert(weatherStation);
		weatherStationsList.add(weatherStation.toDescriptor());
		table.setInput(weatherStation.toDescriptor());
	}
	
	private void editWeatherStation(TableViewer table) {
		
	}
	
	private void deleteWeatherStation(TableViewer table) {
		
	}
	
	private void openClimateCurve(TableViewer table) {
		WeatherStationDescriptor d = Viewers.getFirstSelected(table);
		if (d == null)
			return;
		WeatherStation station = dao.get(d.id);
		ClimateDataChart chart = new ClimateDataChart(UI.shell(), station);
		chart.open();
	}

	private void exportClimateCurve(TableViewer table) {
		WeatherStationDescriptor d = Viewers.getFirstSelected(table);
		if (d == null)
			return;
		WeatherStation station = dao.get(d.id);
		String name = station.name != null ? station.name : "station";
		File file = FileChooser.save(name + ".csv", "*.csv");
		if (file == null)
			return;
		HoursProfile.write(
			file,
			new String[] { "index","temperature","direct radiation","diffuse radiation" },
			station.data,
			station.directRadiation,
			station.diffuseRadiation
		);
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return col == 0 ? Icon.CLIMATE_16.img() : null;
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
				return Num.str(d.longitude);
			case 2:
				return Num.str(d.latitude);
			case 3:
				return Num.str(d.altitude) + " m";
			default:
				return null;
			}
		}
	}

}
