package sophena.rcp.editors.basedata.climate;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import sophena.db.daos.WeatherStationDao;
import sophena.db.usage.SearchResult;
import sophena.db.usage.UsageSearch;
import sophena.io.HoursProfile;
import sophena.model.WeatherStation;
import sophena.model.descriptors.WeatherStationDescriptor;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.colors.Colors;
import sophena.rcp.editors.basedata.BaseTableLabel;
import sophena.rcp.editors.basedata.UsageError;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Num;

class TablePage extends FormPage {

	private final WeatherStationDao dao = new WeatherStationDao(App.getDb());
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

		var label = new Label();
		table.setLabelProvider(label);
		weatherStationsList = dao.getDescriptors();
		Sorters.byName(weatherStationsList);
		table.setInput(weatherStationsList);
		Tables.bindColumnWidths(table, 0.25, 0.25, 0.25, 0.25);
		Tables.sortByLabel(WeatherStationDescriptor.class, table, label, 0);
		Tables.sortByNumber(WeatherStationDescriptor.class, table, d -> d.longitude, 1);
		Tables.sortByNumber(WeatherStationDescriptor.class, table, d -> d.latitude, 2);
		Tables.sortByNumber(WeatherStationDescriptor.class, table, d -> d.altitude, 3);
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
		Action export = Actions.create("Klimadaten exportieren",
				Icon.EXPORT_FILE_16.des(), () -> exportClimateCurve(table));
		Actions.bind(section, add, edit, del, open, export);
		Actions.bind(table, add, edit, del, open, export);
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
		table.setInput(weatherStationsList);
	}

	private void editWeatherStation(TableViewer table) {
		WeatherStationDescriptor d = Viewers.getFirstSelected(table);
		if (d == null)
			return;
		WeatherStation station = dao.get(d.id);
		if (ImportWizard.open(station) != Window.OK)
			return;
		int idx = weatherStationsList.indexOf(d);
		station = dao.update(station);
		weatherStationsList.set(idx, station.toDescriptor());
		table.setInput(weatherStationsList);
	}

	private void deleteWeatherStation(TableViewer table) {
		WeatherStationDescriptor d = Viewers.getFirstSelected(table);
		if (d == null)
			return;
		WeatherStation station = dao.get(d.id);
		if (station == null || station.isProtected)
			return;
		boolean doIt = MsgBox.ask("Wirklich löschen?",
				"Soll die ausgewählte Wetterstation wirklich gelöscht werden?");
		if (!doIt)
			return;
		List<SearchResult> usage = new UsageSearch(App.getDb()).of(station);
		if (!usage.isEmpty()) {
			UsageError.show(usage);
			return;
		}
		dao.delete(station);
		weatherStationsList.remove(d);
		table.setInput(weatherStationsList);
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

	private static class Label extends BaseTableLabel {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (col != 0)
				return null;
			if (!(obj instanceof WeatherStationDescriptor entity))
				return null;
			return entity.isProtected ? Icon.LOCK_16.img() : Icon.CLIMATE_16.img();
		}

		@Override
		public Font getFont(Object obj) {
			if (!(obj instanceof WeatherStationDescriptor entity))
				return null;
			if (entity.isProtected)
				return UI.italicFont();
			return null;
		}

		@Override
		public Color getForeground(Object obj) {
			if (!(obj instanceof WeatherStationDescriptor entity))
				return null;
			if (entity.isProtected)
				return Colors.getDarkGray();
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof WeatherStationDescriptor d))
				return null;
			return switch (col) {
				case 0 -> d.name;
				case 1 -> Double.toString(d.longitude);
				case 2 -> Double.toString(d.latitude);
				case 3 -> Num.str(d.altitude) + " m";
				default -> null;
			};
		}
	}

}
