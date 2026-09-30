package sophena.io.thermos.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.util.Strings;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sophena.io.thermos.ThermosImportConfig;
import sophena.model.Manufacturer;
import sophena.model.TransferStation;
import sophena.rcp.app.App;
import sophena.rcp.colors.Colors;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class TransferStationsPage extends WizardPage {

	private final ThermosImportConfig config;
	private final List<Manufacturer> manufacturers;
	private final List<TransferStation> stations;

	private Text loadRangeText;
	private Text productRangeText;

	private boolean hasConsumers;
	private double consumerMax;

	public TransferStationsPage(ThermosImportConfig config) {
		super("TransferStationsPage", "Hausübergabestationen", null);
		this.config = config;
		setMessage("Wählen Sie einen Hersteller und eine Produktlinie aus.");

		var db = App.getDb();
		this.stations = db.getAll(TransferStation.class);
		Sorters.byName(stations);
		this.manufacturers = new ArrayList<>();
		for (var s : stations) {
			if (!manufacturers.contains(s.manufacturer)) {
				manufacturers.add(s.manufacturer);
			}
		}
		Sorters.byName(manufacturers);
	}

	@Override
	public void createControl(Composite parent) {
		var root = new Composite(parent, SWT.NONE);
		setControl(root);
		UI.gridLayout(root, 1, 5, 5);
		var comp = UI.formComposite(root);
		UI.gridData(comp, true, false);

		loadRangeText = UI.formText(
			comp, "Heizlast der Abnehmer", SWT.READ_ONLY);

		var manCombo = UI.formCombo(comp, "Hersteller");
		var manItems = manufacturers
			.stream()
			.map(m -> m != null ? m.name : "")
			.toArray(String[]::new);
		manCombo.setItems(manItems);
		var lineCombo = UI.formCombo(comp, "Produktlinie");

		productRangeText = UI.formText(
			comp, "Leistungsbereich der Produktlinie", SWT.READ_ONLY);

		Controls.onSelect(manCombo, _ -> {
			int i = manCombo.getSelectionIndex();
			if (i < 0)
				return;
			config.stationManufacturer(manufacturers.get(i));
			var pls = productLinesOf(config.stationManufacturer());
			lineCombo.setItems(pls);
			config.stationProductLine(null);
			refreshProductRange();
			validate();
		});

		Controls.onSelect(lineCombo, _ -> {
			int idx = lineCombo.getSelectionIndex();
			if (idx < 0)
				return;
			config.stationProductLine(lineCombo.getItem(idx));
			refreshProductRange();
			validate();
		});

		refreshLoadRange();
		refreshProductRange();
		validate();
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			refreshLoadRange();
			refreshProductRange();
			validate();
		}
	}

	private String[] productLinesOf(Manufacturer manufacturer) {
		return stations
			.stream()
			.filter(s -> Objects.equals(s.manufacturer, manufacturer))
			.map(s -> s.productLine)
			.distinct()
			.sorted()
			.toArray(String[]::new);
	}

	private void refreshLoadRange() {
		if (loadRangeText == null || loadRangeText.isDisposed())
			return;

		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		boolean any = false;
		for (var c : config.consumersForStationAssignment()) {
			if (c == null || c.heatingLoad <= 0)
				continue;
			min = Math.min(min, c.heatingLoad);
			max = Math.max(max, c.heatingLoad);
			any = true;
		}

		hasConsumers = any;
		consumerMax = any ? max : 0;
		loadRangeText.setText(any ? range(min, max) : "-");
	}

	private void refreshProductRange() {
		if (productRangeText == null || productRangeText.isDisposed())
			return;

		var manufacturer = config.stationManufacturer();
		var productLine = config.stationProductLine();
		if (manufacturer == null || Strings.isBlank(productLine)) {
			productRangeText.setText("");
			productRangeText.setForeground(null);
			return;
		}

		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		boolean any = false;
		for (var s : stations) {
			if (s == null
				|| !Objects.equals(s.manufacturer, manufacturer)
				|| !Objects.equals(s.productLine, productLine))
				continue;
			min = Math.min(min, s.outputCapacity);
			max = Math.max(max, s.outputCapacity);
			any = true;
		}

		if (!any) {
			productRangeText.setText("");
			productRangeText.setForeground(null);
			return;
		}

		productRangeText.setText(range(min, max));
		boolean insufficient = hasConsumers && consumerMax > max;
		productRangeText.setForeground(
			insufficient ? Colors.getChartRed() : null);
	}

	private String range(double min, double max) {
		if (min == max) return Num.str(min) + " kW";
		return Num.str(min) + " kW bis " + Num.str(max) + " kW";
	}

	private void validate() {
		setPageComplete(Strings.isNotBlank(config.stationProductLine()));
	}
}
