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
import sophena.model.Pipe;
import sophena.rcp.App;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.UI;

class PipesPage extends WizardPage {

	private final ThermosImportConfig config;
	private final List<Manufacturer> manufacturers;
	private final List<Pipe> pipes;

	public PipesPage(ThermosImportConfig config) {
		super("PipesPage", "Wärmeleitungen", null);
		this.config = config;
		setMessage("Wählen Sie einen Hersteller und eine Produktlinie aus.");

		var db = App.getDb();
		this.pipes = db.getAll(Pipe.class);
		Sorters.byName(pipes);
		this.manufacturers = new ArrayList<>();
		for (var p : pipes) {
			if (!manufacturers.contains(p.manufacturer)) {
				manufacturers.add(p.manufacturer);
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
		var manItems = manufacturers
			.stream()
			.map(m -> m != null ? m.name : "")
			.toArray(String[]::new);
		manCombo.setItems(manItems);
		var lineCombo = UI.formCombo(comp, "Produktlinie");

		Controls.onSelect(manCombo, $ -> {
			int i = manCombo.getSelectionIndex();
			config.pipeManufacturer(manufacturers.get(i));
			var pls = productLinesOf(config.pipeManufacturer());
			lineCombo.setItems(pls);
			config.pipeProductLine(null);
			validate();
		});

		Controls.onSelect(lineCombo, $ -> {
			int idx = lineCombo.getSelectionIndex();
			config.pipeProductLine(lineCombo.getItem(idx));
			validate();
		});

		validate();
	}

	private String[] productLinesOf(Manufacturer manufacturer) {
		return pipes
			.stream()
			.filter(p -> Objects.equals(p.manufacturer, manufacturer))
			.map(p -> p.productLine)
			.distinct()
			.sorted()
			.toArray(String[]::new);
	}

	private void validate() {
		setPageComplete(Strings.isNotBlank(config.pipeProductLine()));
	}
}
