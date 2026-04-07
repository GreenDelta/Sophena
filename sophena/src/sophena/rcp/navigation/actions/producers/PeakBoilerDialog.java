package sophena.rcp.navigation.actions.producers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.BoilerDao;
import sophena.db.daos.FuelDao;
import sophena.db.daos.HeatPumpDao;
import sophena.db.daos.ProjectDao;
import sophena.model.Boiler;
import sophena.model.CostSettings;
import sophena.model.Fuel;
import sophena.model.FuelGroup;
import sophena.model.FuelSpec;
import sophena.model.HeatPump;
import sophena.model.HeatPumpMode;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.ProductCosts;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.model.Project;
import sophena.rcp.app.App;
import sophena.rcp.app.Icon;
import sophena.rcp.editors.producers.ProducerEditor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Num;
import sophena.utils.Strings;

class PeakBoilerDialog extends FormDialog {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Project project;
	private final double peakDemand;
	private final List<Candidate> candidates;
	private final ProductGroup[] groups;

	private Text demandText;
	private Text rankText;
	private Text functionText;
	private Text nameText;
	private Combo groupCombo;
	private TableViewer table;
	private boolean nameEdited;

	static void open(Project project, double peakDemand) {
		if (project == null || peakDemand <= 0)
			return;
		var dialog = new PeakBoilerDialog(project, peakDemand);
		if (dialog.candidates.isEmpty()) {
			MsgBox.info(
				"Keine passenden Erzeuger gefunden",
				"Für eine benötigte Spitzenlast von " + Num.str(peakDemand)
					+ " kW wurden keine passenden Erzeuger in den Stammdaten gefunden.");
			return;
		}
		dialog.open();
	}

	private PeakBoilerDialog(Project project, double peakDemand) {
		super(UI.shell());
		this.project = project;
		this.peakDemand = peakDemand;
		this.candidates = collectCandidates();
		this.groups = collectGroups();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, false).setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, true);
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		var tk = mform.getToolkit();
		UI.formHeader(mform, "Spitzenlastkessel hinzufügen");
		var body = UI.formBody(mform.getForm(), tk);
		var form = UI.formComposite(body, tk);
		UI.gridData(form, true, false);
		createFields(form, tk);
		createTable(body);
		bindToUi();
	}

	private void createFields(Composite parent, FormToolkit tk) {
		demandText = UI.formText(parent, tk, "Benötigte Leistung");
		demandText.setEditable(false);
		rankText = UI.formText(parent, tk, "Rang");
		rankText.setEditable(false);
		functionText = UI.formText(parent, tk, "Pufferspeicher");
		functionText.setEditable(false);
		nameText = UI.formText(parent, tk, "Name");
		Texts.on(nameText).required().onChanged(t -> {
			var selected = getSelectedCandidate();
			nameEdited = selected == null
					|| !Strings.nullOrEqual(t, selected.name);
			updateOkButton();
		});
		groupCombo = UI.formCombo(parent, tk, "Produktgruppe");
		Controls.onSelect(groupCombo, e -> updateTableInput());
	}

	private void createTable(Composite body) {
		table = Tables.createViewer(body, "Hersteller", "Nennleistung", "Bezeichnung");
		Tables.bindColumnWidths(table, 0.3, 0.25, 0.45);
		table.setLabelProvider(new CandidateLabel());
		table.addSelectionChangedListener(e -> {
			suggestName();
			updateOkButton();
		});
		table.addDoubleClickListener(e -> {
			if (canSave()) {
				okPressed();
			}
		});
	}

	private void bindToUi() {
		demandText.setText(Num.str(peakDemand) + " kW");
		rankText.setText(Integer.toString(maxProducerRank() + 1));
		functionText.setText("Spitzenlast");
		nameEdited = false;
		groupCombo.setItems(groupLabels());
		groupCombo.select(0);
		updateTableInput();
	}

	private void updateTableInput() {
		var filtered = filteredCandidates();
		table.setInput(filtered);
		if (!filtered.isEmpty()) {
			table.setSelection(new StructuredSelection(filtered.get(0)), true);
		} else {
			table.setSelection(StructuredSelection.EMPTY);
			if (!nameEdited) {
				nameText.setText("");
			}
		}
		updateOkButton();
	}

	private List<Candidate> filteredCandidates() {
		var group = selectedGroup();
		if (group == null)
			return candidates;
		var filtered = new ArrayList<Candidate>();
		for (var candidate : candidates) {
			if (candidate.group == group) {
				filtered.add(candidate);
			}
		}
		return filtered;
	}

	private void suggestName() {
		if (nameEdited && !Texts.isEmpty(nameText))
			return;
		var selected = getSelectedCandidate();
		nameText.setText(selected != null ? selected.name : "");
	}

	private void updateOkButton() {
		var ok = getButton(IDialogConstants.OK_ID);
		if (ok != null) {
			ok.setEnabled(canSave());
		}
	}

	private boolean canSave() {
		return getSelectedCandidate() != null && !Texts.isEmpty(nameText);
	}

	@Override
	protected void okPressed() {
		var candidate = getSelectedCandidate();
		if (candidate == null)
			return;
		try {
			var producer = new Producer();
			producer.id = UUID.randomUUID().toString();
			producer.name = nameText.getText().trim();
			producer.rank = maxProducerRank() + 1;
			producer.function = ProducerFunction.PEAK_LOAD;
			producer.productGroup = candidate.group;
			producer.boiler = candidate.boiler;
			producer.heatPump = candidate.heatPump;
			initFuelSpec(producer);
			initCosts(producer);
			initElectricity(producer);
			initHeatPump(producer);
			project.producers.add(producer);
			new ProjectDao(App.getDb()).update(project);
			Navigator.refresh();
			ProducerEditor.open(project.toDescriptor(), producer.toDescriptor());
			close();
		} catch (Exception e) {
			log.error("failed to create peak load producer", e);
			MsgBox.error("Fehler", "Der Spitzenlastkessel konnte nicht angelegt werden.");
		}
	}

	@Override
	protected Point getInitialSize() {
		int width = 700;
		int height = 620;
		Rectangle shellBounds = getShell().getDisplay().getBounds();
		int shellWidth = shellBounds.x;
		int shellHeight = shellBounds.y;
		if (shellWidth > 0 && shellWidth < width)
			width = shellWidth;
		if (shellHeight > 0 && shellHeight < height)
			height = shellHeight;
		return new Point(width, height);
	}

	@Override
	protected Point getInitialLocation(Point initialSize) {
		Point loc = super.getInitialLocation(initialSize);
		int marginTop = (getParentShell().getSize().y - initialSize.y) / 3;
		if (marginTop < 0)
			marginTop = 0;
		return new Point(loc.x, loc.y + marginTop);
	}

	private ProductGroup selectedGroup() {
		int idx = groupCombo.getSelectionIndex();
		if (idx <= 0 || idx > groups.length)
			return null;
		return groups[idx - 1];
	}

	private String[] groupLabels() {
		String[] items = new String[groups.length + 1];
		items[0] = "";
		for (int i = 0; i < groups.length; i++) {
			items[i + 1] = groups[i].name;
		}
		return items;
	}

	private Candidate getSelectedCandidate() {
		return Viewers.getFirstSelected(table);
	}

	private int maxProducerRank() {
		int max = 0;
		for (var producer : project.producers) {
			max = Math.max(max, producer.rank);
		}
		return max;
	}

	private List<Candidate> collectCandidates() {
		var candidates = new ArrayList<Candidate>();
		for (var boiler : new BoilerDao(App.getDb()).getAll()) {
			if (boiler == null || boiler.group == null || boiler.group.type == null)
				continue;
			if (!isSelectableBoilerGroup(boiler.group.type))
				continue;
			double power = Math.max(0, boiler.maxPower);
			if (power < peakDemand)
				continue;
			candidates.add(Candidate.of(boiler, power));
		}
		for (var heatPump : new HeatPumpDao(App.getDb()).getAll()) {
			if (heatPump == null || heatPump.group == null)
				continue;
			if (heatPump.group.type != ProductType.HEAT_PUMP)
				continue;
			double power = Math.max(0, heatPump.ratedPower);
			if (power < peakDemand)
				continue;
			candidates.add(Candidate.of(heatPump, power));
		}
		candidates.sort(Comparator
				.comparingDouble((Candidate candidate) -> candidate.power)
				.thenComparing(candidate -> Strings.nullOrEmpty(candidate.manufacturer)
						? ""
						: candidate.manufacturer)
				.thenComparing(candidate -> Strings.nullOrEmpty(candidate.name)
						? ""
						: candidate.name));
		return candidates;
	}

	private ProductGroup[] collectGroups() {
		var set = new HashSet<ProductGroup>();
		for (var candidate : candidates) {
			if (candidate.group != null) {
				set.add(candidate.group);
			}
		}
		var groups = new ArrayList<>(set);
		groups.sort(Comparator.comparing(group -> Strings.nullOrEmpty(group.name)
				? ""
				: group.name));
		return groups.toArray(new ProductGroup[0]);
	}

	private boolean isSelectableBoilerGroup(ProductType type) {
		return type == ProductType.BIOMASS_BOILER
				|| type == ProductType.FOSSIL_FUEL_BOILER
				|| type == ProductType.COGENERATION_PLANT;
	}

	private void initFuelSpec(Producer producer) {
		producer.fuelSpec = new FuelSpec();
		if (producer.productGroup == null || producer.productGroup.fuelGroup == null)
			return;
		FuelGroup group = producer.productGroup.fuelGroup;
		if (group == FuelGroup.ELECTRICITY) {
			CostSettings settings = project.costSettings;
			if (settings != null && settings.electricityMix != null) {
				producer.fuelSpec.fuel = settings.electricityMix;
				return;
			}
		}
		for (Fuel fuel : new FuelDao(App.getDb()).getAll()) {
			if (fuel.group != group)
				continue;
			producer.fuelSpec.fuel = fuel;
			if (fuel.isProtected)
				break;
		}
	}

	private void initCosts(Producer producer) {
		producer.costs = new ProductCosts();
		if (producer.boiler != null) {
			ProductCosts.copy(producer.boiler, producer.costs);
		} else if (producer.heatPump != null) {
			ProductCosts.copy(producer.heatPump, producer.costs);
		} else if (producer.productGroup != null) {
			ProductCosts.copy(producer.productGroup, producer.costs);
		}
		producer.heatRecoveryCosts = new ProductCosts();
	}

	private void initElectricity(Producer producer) {
		if (producer.productGroup == null
				|| producer.productGroup.type != ProductType.COGENERATION_PLANT) {
			return;
		}
		if (project.costSettings != null
				&& project.costSettings.replacedElectricityMix != null) {
			producer.producedElectricity = project.costSettings.replacedElectricityMix;
			return;
		}
		producer.producedElectricity = new FuelDao(App.getDb())
				.getAll().stream()
				.filter(fuel -> fuel.group == FuelGroup.ELECTRICITY)
				.findFirst()
				.orElse(null);
	}

	private void initHeatPump(Producer producer) {
		if (producer.productGroup == null
				|| producer.productGroup.type != ProductType.HEAT_PUMP) {
			return;
		}
		if (producer.productGroup.name != null
				&& producer.productGroup.name.contains("Luft")) {
			producer.heatPumpMode = HeatPumpMode.OUTODOOR_TEMPERATURE_MODE;
		} else {
			producer.heatPumpMode = HeatPumpMode.USER_TEMPERATURE_MODE;
			producer.sourceTemperatureUser = 0d;
		}
	}

	private static class Candidate {

		final Boiler boiler;
		final HeatPump heatPump;
		final ProductGroup group;
		final String manufacturer;
		final String name;
		final double power;

		private Candidate(Boiler boiler, HeatPump heatPump, ProductGroup group,
				String manufacturer, String name, double power) {
			this.boiler = boiler;
			this.heatPump = heatPump;
			this.group = group;
			this.manufacturer = manufacturer;
			this.name = name;
			this.power = power;
		}

		static Candidate of(Boiler boiler, double power) {
			String manufacturer = boiler.manufacturer != null
					? boiler.manufacturer.name
					: null;
			return new Candidate(boiler, null, boiler.group, manufacturer,
					boiler.name, power);
		}

		static Candidate of(HeatPump heatPump, double power) {
			String manufacturer = heatPump.manufacturer != null
					? heatPump.manufacturer.name
					: null;
			return new Candidate(null, heatPump, heatPump.group, manufacturer,
					heatPump.name, power);
		}
	}

	private static class CandidateLabel extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object elem, int col) {
			return col == 0 ? Icon.BOILER_16.img() : null;
		}

		@Override
		public String getColumnText(Object elem, int col) {
			if (!(elem instanceof Candidate candidate))
				return null;
			return switch (col) {
				case 0 -> candidate.manufacturer;
				case 1 -> Num.str(candidate.power) + " kW";
				case 2 -> candidate.name;
				default -> null;
			};
		}
	}
}
