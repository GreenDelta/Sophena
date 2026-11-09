package sophena.rcp.editors.biogas.plant;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.calc.biogas.FermenterHeatDemand;
import sophena.db.daos.WeatherStationDao;
import sophena.model.WeatherStation;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.Fermenter;
import sophena.model.biogas.RoofType;
import sophena.model.descriptors.WeatherStationDescriptor;
import sophena.rcp.M;
import sophena.rcp.app.App;
import sophena.rcp.app.Icon;
import sophena.rcp.editors.producers.biogas.HeatDemandChart;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Num;

/// Shows the fermenters of a biogas plant in a table and the heat demand of
/// the fermenters for a selectable climate region. The climate region is only
/// used for this preview and is not stored in the plant; in a project, the
/// weather station of the project is used.
class FermenterPage extends FormPage {

	/// The last selected weather station; it is kept for the session so that
	/// it does not need to be selected again in other editors.
	private static WeatherStation lastStation;

	private final BiogasPlantEditor editor;
	private TableViewer table;

	private WeatherStation station;
	private double ratedPower;
	private HeatDemandChart chart;
	private Text annualDemandText;
	private Text peakDemandText;

	FermenterPage(BiogasPlantEditor editor) {
		super(editor, "FermenterPage", "Fermenter");
		this.editor = editor;
	}

	private BiogasPlant plant() {
		return editor.plant();
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		var form = UI.formHeader(mForm, "Fermenter");
		var tk = mForm.getToolkit();
		var body = UI.formBody(form, tk);
		createTableSection(body, tk);
		createHeatDemandSection(body, tk);
		form.reflow(true);

		// the rated power of the plant is needed for double membrane roofs
		editor.onResult(r -> {
			ratedPower = r.ratedPower();
			updateHeatDemand();
		});
		editor.calculate();
	}

	// region fermenter table

	private void createTableSection(Composite body, FormToolkit tk) {
		var section = UI.section(body, tk, "Fermenter");
		var comp = UI.sectionClient(section, tk);
		table = Tables.createViewer(comp,
			"Name",
			"Nachfermenter",
			"Solltemperatur",
			"Volumen",
			"Dachtyp");
		Tables.bindColumnWidths(table, 0.25, 0.25, 0.15, 0.15, 0.20);
		Tables.rightAlignColumns(table, 2, 3);
		table.setLabelProvider(new FermenterLabel());

		var add = Actions.create(M.Add, Icon.ADD_16.des(), this::add);
		var edit = Actions.create(M.Edit, Icon.EDIT_16.des(), this::edit);
		var remove = Actions.create(
			M.Remove, Icon.DELETE_16.des(), this::remove);
		Actions.bind(section, add, edit, remove);
		Actions.bind(table, add, edit, remove);
		Tables.onDoubleClick(table, _ -> edit());
		Tables.onDeletePressed(table, _ -> remove());
		table.setInput(plant().fermenters);
	}

	private void add() {
		var name = plant().fermenters.isEmpty()
			? "Hauptfermenter"
			: "Fermenter " + (plant().fermenters.size() + 1);
		var fermenter = Fermenter.createDefault(name);
		if (FermenterWizard.open(fermenter, plant().fermenters) != Window.OK)
			return;
		plant().fermenters.add(fermenter);
		changed();
	}

	private void edit() {
		Fermenter selected = Viewers.getFirstSelected(table);
		if (selected == null)
			return;
		// edit a copy with the same ID so that the dialog can be cancelled
		var clone = selected.copy();
		clone.id = selected.id;
		if (FermenterWizard.open(clone, plant().fermenters) != Window.OK)
			return;
		selected.setValuesOf(clone);
		changed();
	}

	private void remove() {
		List<Fermenter> selected = Viewers.getAllSelected(table);
		if (selected.isEmpty())
			return;
		plant().fermenters.removeAll(selected);
		// remove references to the deleted fermenters
		for (var f : plant().fermenters) {
			for (var s : selected) {
				if (Objects.equals(f.successorId, s.id)) {
					f.successorId = null;
					break;
				}
			}
		}
		changed();
	}

	private void changed() {
		table.setInput(plant().fermenters);
		editor.setDirty();
		updateHeatDemand();
	}

	private class FermenterLabel extends LabelProvider
		implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof Fermenter f))
				return null;
			return switch (col) {
				case 0 -> FermenterWizard.labelOf(f);
				case 1 -> successorLabelOf(f);
				case 2 -> Num.str(f.targetTemperature, 1) + " °C";
				case 3 -> Num.intStr(f.volume()) + " m³";
				case 4 -> f.roofType == RoofType.FIXED
					? "Festes Dach"
					: "Doppelmembrandach";
				default -> null;
			};
		}

		private String successorLabelOf(Fermenter f) {
			if (f.successorId == null)
				return "-";
			for (var other : plant().fermenters) {
				if (Objects.equals(other.id, f.successorId))
					return FermenterWizard.labelOf(other);
			}
			return "-";
		}
	}

	// endregion

	// region heat demand

	private void createHeatDemandSection(Composite body, FormToolkit tk) {
		var section = UI.section(body, tk, "Wärmebedarf der Fermenter");
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);

		var info = tk.createComposite(comp);
		UI.innerGrid(info, 3);
		UI.gridData(info, true, false);
		createStationCombo(info, tk);
		annualDemandText = readOnlyText(info, tk, "Jahresbedarf");
		UI.formLabel(info, tk, "MWh/a");
		peakDemandText = readOnlyText(info, tk, "Maximale Leistung");
		UI.formLabel(info, tk, "kW");

		chart = HeatDemandChart.create(comp);
	}

	private void createStationCombo(Composite comp, FormToolkit tk) {
		var dao = new WeatherStationDao(App.getDb());
		var stations = dao.getDescriptors();
		Sorters.byName(stations);
		var combo = new EntityCombo<WeatherStationDescriptor>()
			.create("Klimaregion", comp, tk)
			.setInput(stations);
		UI.filler(comp, tk);

		// select the last station of the session or the first station
		station = lastStation;
		if (station == null && !stations.isEmpty()) {
			station = dao.get(stations.getFirst().id);
		}
		if (station != null) {
			combo.select(station.toDescriptor());
		}

		combo.onSelect(d -> {
			station = dao.get(d.id);
			lastStation = station;
			updateHeatDemand();
		});
	}

	private Text readOnlyText(Composite comp, FormToolkit tk, String label) {
		var text = UI.formText(comp, tk, label);
		text.setEditable(false);
		return text;
	}

	/// Recalculates the heat demand of the fermenters. The chart is empty when
	/// no substrates or fermenters are defined or the simulation fails for
	/// another reason.
	private void updateHeatDemand() {
		if (chart == null)
			return;
		var res = FermenterHeatDemand.of(station, plant(), ratedPower);
		var data = res.isError() ? null : res.value();
		double annual = chart.update(data);
		if (data == null) {
			Texts.set(annualDemandText, "-");
			Texts.set(peakDemandText, "-");
			annualDemandText.setToolTipText(res.error());
			return;
		}
		annualDemandText.setToolTipText(null);
		Texts.set(annualDemandText, Num.intStr(annual / 1000));
		double peak = 0;
		for (double d : data) {
			peak = Math.max(peak, d);
		}
		Texts.set(peakDemandText, Num.intStr(peak));
	}

	// endregion
}
