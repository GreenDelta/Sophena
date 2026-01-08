package sophena.io.thermos.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.util.Strings;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import sophena.io.thermos.ThermosImportConfig;
import sophena.model.Manufacturer;
import sophena.model.TransferStation;
import sophena.rcp.App;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.UI;

class TransferStationsPage extends WizardPage {

	private final ThermosImportConfig config;
	private final List<Manufacturer> manufacturers;
	private final List<TransferStation> stations;

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

		var manCombo = UI.formCombo(comp, "Hersteller");
		var manItems = manufacturers.stream()
			.map(m -> m != null ? m.name : "")
			.toArray(String[]::new);
		manCombo.setItems(manItems);
		var lineCombo = UI.formCombo(comp, "Produktlinie");

		Controls.onSelect(manCombo, $ -> {
			int i = manCombo.getSelectionIndex();
			config.stationManufacturer(manufacturers.get(i));
			var pls = productLinesOf(config.stationManufacturer());
			lineCombo.setItems(pls);
			config.stationProductLine(null);
			validate();
		});

		Controls.onSelect(lineCombo, $ -> {
			int idx = lineCombo.getSelectionIndex();
			config.stationProductLine(lineCombo.getItem(idx));
			validate();
		});

		validate();
	}

	private String[] productLinesOf(Manufacturer manufacturer) {
		return stations.stream()
			.filter(s -> Objects.equals(s.manufacturer, manufacturer))
			.map(s -> s.productLine)
			.distinct()
			.sorted()
			.toArray(String[]::new);
	}

	private void validate() {
		setPageComplete(Strings.isNotBlank(config.stationProductLine()));
	}
}
