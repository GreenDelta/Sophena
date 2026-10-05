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
import sophena.io.thermos.pipes.PipeSync;
import sophena.model.Manufacturer;
import sophena.model.Pipe;
import sophena.rcp.app.App;
import sophena.rcp.colors.Colors;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

/// The page for selecting a matching product line for the network pipes.
class PipesPage extends WizardPage {

	private static final String INFO =
		"Wählen Sie einen Hersteller und eine Produktlinie aus.";

	private final ThermosImportConfig config;
	private final List<Manufacturer> manufacturers;
	private final List<Pipe> pipes;

	private Text diameterRangeText;

	public PipesPage(ThermosImportConfig config) {
		super("PipesPage", "Wärmeleitungen", null);
		this.config = config;
		setMessage(INFO);

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

		diameterRangeText = UI.formText(
			comp, "Nennweitenbereich der Produktlinie", SWT.READ_ONLY);

		Controls.onSelect(manCombo, _ -> {
			int i = manCombo.getSelectionIndex();
			if (i < 0)
				return;
			config.pipeManufacturer(manufacturers.get(i));
			var pls = productLinesOf(config.pipeManufacturer());
			lineCombo.setItems(pls);
			config.pipeProductLine(null);
			config.skipPipes(false);
			refreshDiameterRange();
			validate();
		});

		Controls.onSelect(lineCombo, _ -> {
			int idx = lineCombo.getSelectionIndex();
			if (idx < 0)
				return;
			config.pipeProductLine(lineCombo.getItem(idx));
			config.skipPipes(false);
			refreshDiameterRange();
			validate();
		});

		refreshDiameterRange();
		validate();
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			refreshDiameterRange();
			validate();
		}
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

	private void refreshDiameterRange() {
		if (diameterRangeText == null || diameterRangeText.isDisposed())
			return;

		var available = config.pipesForProductLine(pipes);
		if (available.isEmpty()) {
			diameterRangeText.setText("");
			diameterRangeText.setForeground(null);
			diameterRangeText.setToolTipText(null);
			setErrorMessage(null);
			setMessage(INFO);
			return;
		}

		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		for (var p : available) {
			min = Math.min(min, p.innerDiameter);
			max = Math.max(max, p.innerDiameter);
		}
		diameterRangeText.setText(range(min, max));

		var err = PipeSync.check(config, available);
		if (err.isError()) {
			diameterRangeText.setToolTipText(err.error());
			diameterRangeText.setForeground(Colors.getChartRed());
			setMessage(null);
			setErrorMessage("Mit der ausgewählte Produktlinie können nicht alle " +
				"Netzabschnitte dimensioniert werden.");
		} else {
			diameterRangeText.setToolTipText(null);
			diameterRangeText.setForeground(null);
			setErrorMessage(null);
			setMessage(INFO);
		}
	}

	private String range(double min, double max) {
		return min == max
			? Num.str(min) + " mm"
			: Num.str(min) + " mm bis " + Num.str(max) + " mm";
	}

	private void validate() {
		setPageComplete(Strings.isNotBlank(config.pipeProductLine()));
	}
}
