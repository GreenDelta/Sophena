package sophena.rcp.editors.biogas.results;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.ToDoubleFunction;

import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.Labels;
import sophena.calc.biogas.biomethane.BiomethaneCalculator;
import sophena.calc.biogas.biomethane.BiomethaneInput;
import sophena.calc.biogas.biomethane.BiomethaneInput.PlantInvestment;
import sophena.calc.biogas.biomethane.BiomethaneResult;
import sophena.model.biogas.BiogasPlant;
import sophena.rcp.app.Icon;
import sophena.rcp.colors.Colors;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpLink;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

/// Shows a rough economic evaluation of upgrading the biogas of the plant to
/// biomethane and selling it instead of generating electricity and heat. The
/// gas volume comes from the substrates of the plant; all other values are
/// adjustable defaults (see `BiomethaneInput`) that are not stored. The result
/// is recalculated directly when an input is changed.
class BiomethanePage extends FormPage {

	private static final String INTRO_TEXT = """
		Grundsätzlich kann das Biogas auch zu Biomethan aufbereitet und \
		über das Erdgasnetz vermarktet werden. Nachfolgend sind die \
		Ergebnisse einer Kalkulation dieses Szenarios nach der \
		Annuitätenmethode (VDI 2067) dargestellt. Die Biogasmenge wird aus \
		den Substraten der Anlage berechnet. Alle weiteren Werte sind \
		Annahmen mit Standardwerten, die angepasst werden können. Die \
		Investition \
		der Aufbereitung ist abhängig von der Biogasmenge, die laufenden \
		Kosten von der Rohgasenergie. Für den Gasnetzanschluss werden \
		standardmäßig pauschal 250.000 EUR angesetzt. Förderprogramme können als \
		Investitionsförderung in den Kostenparametern berücksichtigt \
		werden. Der spezifische Methanverkaufspreis hängt vom \
		Einsatzstoffmix ab und ist Marktschwankungen unterworfen. \
		Lieferverträge haben selten Laufzeiten von mehr als fünf Jahren. \
		Daher kann die Kalkulation nur eine ungefähre Abschätzung \
		darstellen. Das bei der Aufbereitung anfallende CO2 kann ebenfalls \
		vermarktet werden. Hierfür sind allerdings zusätzliche Anschaffungen \
		erforderlich, die hier nicht berücksichtigt sind. (Stand: 2026)""";

	private final BiogasPlant plant;

	/// The default values that are used to mark modified inputs.
	private BiomethaneInput defaults;
	private BiomethaneInput input;
	private BiomethaneResult result;

	/// Functions that write the current input values into the input fields;
	/// these are called when the defaults are restored.
	private final List<Runnable> fieldUpdates = new ArrayList<>();

	private ScrolledForm form;
	private TableViewer gasTable;
	private TableViewer resultTable;

	BiomethanePage(BiogasResultEditor editor) {
		super(editor, "sophena.BiomethanePage",
			"Alternative Biomethanaufbereitung");
		this.plant = editor.result().plant();
		this.defaults = BiomethaneInput.of(plant);
		this.input = BiomethaneInput.of(plant);
		this.result = calculate();
	}

	private BiomethaneResult calculate() {
		return BiomethaneCalculator.calculate(plant, input);
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		form = UI.formHeader(mform, "Alternative Biomethanaufbereitung");
		var tk = mform.getToolkit();
		var body = UI.formBody(form, tk);

		var reset = Actions.create("Standardwerte wiederherstellen",
			Icon.REFRESH_16.des(), this::resetInputs);
		form.getToolBarManager().add(reset);
		form.updateToolBar();

		createIntroText(body, tk);
		createUpgradingSection(body, tk);
		createGridSection(body, tk);
		createPlantInvestmentSection(body, tk);
		createRevenueSection(body, tk);
		createCostSection(body, tk);
		createPriceChangeSection(body, tk);
		createGasSection(body, tk);
		createResultSection(body, tk);

		updateResults();
		form.reflow(true);
	}

	private void createIntroText(Composite body, FormToolkit tk) {
		var label = tk.createLabel(body, INTRO_TEXT, SWT.WRAP);
		// a small width hint is needed so that the label wraps its text
		// instead of growing to the width of the text
		UI.gridData(label, true, false).widthHint = 200;
	}

	// region input sections

	private void createUpgradingSection(Composite parent, FormToolkit tk) {
		var comp = inputSection(parent, tk, "Biogasaufbereitung", false);
		num(comp, tk, "Vollbenutzungsstunden", "h/a",
			i -> i.fullLoadHours, v -> input.fullLoadHours = v,
			H.BiomethaneFullLoadHoursInfo);
		num(comp, tk, "Spezifische Investition", "EUR/(m³/h)",
			i -> i.specificInvestment, v -> input.specificInvestment = v,
			H.BiomethaneSpecificInvestmentInfo);
		investmentFields(comp, tk, i -> i.upgrading);
		num(comp, tk, "Laufende Kosten", "EUR/MWh",
			i -> i.runningCosts, v -> input.runningCosts = v,
			H.BiomethaneRunningCostsInfo);
		num(comp, tk, "Methanverlust der Aufbereitung", "%",
			i -> i.methaneLoss, v -> input.methaneLoss = v,
			H.BiomethaneMethaneLossInfo);
	}

	private void createGridSection(Composite parent, FormToolkit tk) {
		var comp = inputSection(parent, tk, "Gasnetzanschluss", false);
		num(comp, tk, "Investition", "EUR",
			i -> i.gridInvestment, v -> input.gridInvestment = v,
			H.BiomethaneGridConnectionInfo);
		investmentFields(comp, tk, i -> i.grid);
	}

	/// Creates the fields for the lifetime and the operation related costs of
	/// a new investment of the upgrading option.
	private void investmentFields(
		Composite comp, FormToolkit tk,
		Function<BiomethaneInput, BiomethaneInput.InvestmentInput> fn
	) {
		integer(comp, tk, "Nutzungsdauer", "a",
			i -> fn.apply(i).lifetime,
			v -> fn.apply(input).lifetime = v);
		num(comp, tk, "Instandsetzung", "%",
			i -> fn.apply(i).repair,
			v -> fn.apply(input).repair = v, null);
		num(comp, tk, "Wartung und Inspektion", "%",
			i -> fn.apply(i).maintenance,
			v -> fn.apply(input).maintenance = v, null);
		num(comp, tk, "Bedienaufwand", "h/a",
			i -> fn.apply(i).operation,
			v -> fn.apply(input).operation = v, null);
	}

	private void createPlantInvestmentSection(
		Composite parent, FormToolkit tk
	) {
		var section = UI.section(parent, tk, "Investitionen der Biogasanlage");
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		var table = Tables.createViewer(comp,
			"Einbeziehen", "Produktbereich", "Produkt",
			"Investitionskosten", "Nutzungsdauer");
		Tables.bindColumnWidths(table, 0.12, 0.25, 0.33, 0.15, 0.15);
		Tables.rightAlignColumns(table, 3, 4);
		table.setLabelProvider(new InvestmentLabel());
		setRows(table, input.plantInvestments);
		fieldUpdates.add(() -> table.setInput(input.plantInvestments));

		// toggle the inclusion of an investment with a click on the check box
		Tables.onClick(table, e -> {
			var item = Tables.getItem(table, e);
			if (item == null
				|| !item.getBounds(0).contains(e.x, e.y)
				|| !(item.getData() instanceof PlantInvestment pi))
				return;
			pi.included = !pi.included;
			table.refresh();
			updateResults();
		});
	}

	private void createRevenueSection(Composite parent, FormToolkit tk) {
		var comp = inputSection(parent, tk, "Erlöse", false);
		num(comp, tk, "Verkaufspreis Biomethan aus Wirtschaftsdünger",
			"EUR/kWh", i -> i.manurePrice, v -> input.manurePrice = v,
			H.ManureBiomethanePriceInfo);
		num(comp, tk, "Verkaufspreis Biomethan aus Nicht-Wirtschaftsdünger",
			"EUR/kWh", i -> i.nonManurePrice, v -> input.nonManurePrice = v,
			H.NonManureBiomethanePriceInfo);
	}

	private void createCostSection(Composite parent, FormToolkit tk) {
		var comp = inputSection(parent, tk, "Kostenparameter", true);
		integer(comp, tk, "Betrachtungszeitraum", "a",
			i -> i.duration, v -> input.duration = v);
		num(comp, tk, "Kapital-Mischzinssatz", "%",
			i -> i.interestRate, v -> input.interestRate = v, null);
		num(comp, tk, "Mittlerer Stundenlohn", "EUR",
			i -> i.hourlyWage, v -> input.hourlyWage = v, null);
		num(comp, tk, "Strompreis", "EUR/kWh",
			i -> i.electricityPrice, v -> input.electricityPrice = v, null);
		num(comp, tk, "Durchschnittlicher Eigenstrombedarf", "kW",
			i -> i.avgPowerDemand, v -> input.avgPowerDemand = v,
			H.BiomethanePowerDemandInfo);
		num(comp, tk, "Versicherung", "%",
			i -> i.insuranceCostsShare, v -> input.insuranceCostsShare = v,
			null);
		num(comp, tk, "Sonstige Abgaben (Steuern, Pacht, usw.)", "%",
			i -> i.otherCostsShare, v -> input.otherCostsShare = v, null);
		num(comp, tk, "Investitionsförderung", "EUR",
			i -> i.funding, v -> input.funding = v, null);
	}

	private void createPriceChangeSection(Composite parent, FormToolkit tk) {
		var comp = inputSection(parent, tk, "Preisänderungsfaktoren", true);
		num(comp, tk, "Investitionen", "",
			i -> i.investmentFactor, v -> input.investmentFactor = v, null);
		num(comp, tk, "Biomasse-Brennstoff", "",
			i -> i.bioFuelFactor, v -> input.bioFuelFactor = v, null);
		num(comp, tk, "Strom", "",
			i -> i.electricityFactor, v -> input.electricityFactor = v, null);
		num(comp, tk, "Lohnkosten und sonstige Kosten", "",
			i -> i.operationFactor, v -> input.operationFactor = v, null);
		num(comp, tk, "Instandhaltung", "",
			i -> i.maintenanceFactor, v -> input.maintenanceFactor = v, null);
		num(comp, tk, "Laufende Kosten der Aufbereitung", "",
			i -> i.runningCostsFactor, v -> input.runningCostsFactor = v, null);
		num(comp, tk, "Biomethanerlöse", "",
			i -> i.revenuesFactor, v -> input.revenuesFactor = v, null);
	}

	private Composite inputSection(
		Composite parent, FormToolkit tk, String title, boolean collapsed
	) {
		var section = collapsed
			? UI.collapsedSection(parent, tk, title)
			: UI.section(parent, tk, title);
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 4);
		return comp;
	}

	/// Creates a row with a decimal input field. The getter takes the input
	/// object as parameter so that it can be applied on the defaults and on the
	/// current input.
	private void num(
		Composite comp, FormToolkit tk, String label, String unit,
		ToDoubleFunction<BiomethaneInput> getter,
		DoubleConsumer setter,
		String helpKey
	) {
		Text text = UI.formText(comp, tk, label);
		UI.formLabel(comp, tk, unit);
		if (helpKey != null) {
			HelpLink.create(comp, tk, label, helpKey);
		} else {
			UI.filler(comp, tk);
		}
		Runnable mark = () -> markModified(text,
			getter.applyAsDouble(input), getter.applyAsDouble(defaults));
		Texts.on(text)
			.decimal()
			.init(getter.applyAsDouble(input))
			.onChanged(s -> {
				setter.accept(Num.read(s));
				mark.run();
				updateResults();
			});
		fieldUpdates.add(() -> {
			Texts.set(text, getter.applyAsDouble(input));
			mark.run();
		});
	}

	/// Creates a row with an integer input field.
	private void integer(
		Composite comp, FormToolkit tk, String label, String unit,
		ToDoubleFunction<BiomethaneInput> getter,
		IntConsumer setter
	) {
		Text text = UI.formText(comp, tk, label);
		UI.formLabel(comp, tk, unit);
		UI.filler(comp, tk);
		Runnable mark = () -> markModified(text,
			getter.applyAsDouble(input), getter.applyAsDouble(defaults));
		Texts.on(text)
			.integer()
			.init((int) getter.applyAsDouble(input))
			.onChanged(s -> {
				setter.accept(Num.readInt(s));
				mark.run();
				updateResults();
			});
		fieldUpdates.add(() -> {
			Texts.set(text, (int) getter.applyAsDouble(input));
			mark.run();
		});
	}

	private static void markModified(Text text, double value, double initial) {
		text.setBackground(Math.abs(value - initial) > 1e-9
			? Colors.forModifiedDefault()
			: Colors.getWhite());
	}

	private void resetInputs() {
		defaults = BiomethaneInput.of(plant);
		input = BiomethaneInput.of(plant);
		for (var update : fieldUpdates) {
			update.run();
		}
		updateResults();
	}

	// endregion

	// region result sections

	private void createResultSection(Composite parent, FormToolkit tk) {
		var section = UI.section(parent, tk, "Wirtschaftlichkeit");
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		resultTable = Tables.createViewer(comp, "", "dynamisch", "statisch");
		Tables.bindColumnWidths(resultTable, 0.5, 0.25, 0.25);
		Tables.rightAlignColumns(resultTable, 1, 2);
		resultTable.setLabelProvider(new ItemLabel());
	}

	private void createGasSection(Composite parent, FormToolkit tk) {
		var section = UI.section(parent, tk, "Gasmenge und Auslegung");
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		gasTable = Tables.createViewer(comp, "", "Wert", "Einheit");
		Tables.bindColumnWidths(gasTable, 0.5, 0.3, 0.2);
		Tables.rightAlignColumns(gasTable, 1);
		gasTable.setLabelProvider(new ItemLabel());
	}

	private void updateResults() {
		result = calculate();
		if (resultTable == null)
			return;
		setRows(resultTable, resultItems());
		setRows(gasTable, gasItems());
	}

	/// Sets the rows of the given table and adjusts its height to the rows.
	private static void setRows(TableViewer viewer, List<?> rows) {
		viewer.setInput(rows);
		var table = viewer.getTable();
		if (table.getLayoutData() instanceof GridData data) {
			int height = table.getHeaderHeight()
				+ (rows.size() + 1) * table.getItemHeight();
			data.heightHint = Math.max(height, 60);
			data.minimumHeight = data.heightHint;
		}
	}

	private List<Item> gasItems() {
		var r = result;
		var items = new ArrayList<Item>();
		items.add(Item.of("Biogasmenge (Normvolumen)",
			Num.intStr(r.biogasVolume), "m³/a"));
		items.add(Item.of("Methanmenge (Normvolumen)",
			Num.intStr(r.methaneVolume), "m³/a"));
		items.add(Item.of("Methangehalt",
			Num.str(100 * r.methaneContent(), 1), "%"));
		items.add(Item.of("Rohgasenergie",
			Num.intStr(r.rawGasEnergy / 1000), "MWh/a"));
		items.add(Item.of("Kapazität der Aufbereitung",
			Num.str(r.capacity, 1), "m³/h"));
		items.add(Item.of("Investition der Aufbereitung",
			Num.intStr(r.upgradingInvestment), "EUR"));
		items.add(Item.of("Verkaufte Biomethanmenge",
			Num.intStr(r.soldEnergy() / 1000), "MWh/a"));
		items.add(Item.of("Mischpreis Biomethan",
			Num.str(100 * r.mixedPrice(), 2), "ct/kWh"));
		return items;
	}

	private List<Item> resultItems() {
		var dyn = result.dynamicTotal;
		var stat = result.staticTotal;
		var items = new ArrayList<Item>();

		items.add(Item.of("Investitionskosten", "EUR",
			dyn.investments, stat.investments));
		items.add(Item.of("Investitionsförderung", "EUR",
			dyn.investmentFunding, stat.investmentFunding));
		items.add(Item.of("Finanzierungsbedarf", "EUR",
			dyn.investments - dyn.investmentFunding,
			stat.investments - stat.investmentFunding).bold());
		items.add(new Item());

		items.add(Item.of("Kapitalgebundene Kosten", "EUR/a",
			dyn.capitalCosts, stat.capitalCosts));
		items.add(Item.of("Bedarfsgebundene Kosten", "EUR/a",
			dyn.consumptionCosts, stat.consumptionCosts));
		items.add(Item.of("Betriebsgebundene Kosten", "EUR/a",
			dyn.operationCosts, stat.operationCosts));
		items.add(Item.of("Sonstige Kosten", "EUR/a",
			dyn.otherAnnualCosts, stat.otherAnnualCosts));
		items.add(Item.of("Gesamtkosten", "EUR/a",
			dyn.totalAnnualCosts, stat.totalAnnualCosts).bold());
		items.add(new Item());

		items.add(Item.of("Erlöse Biomethan aus Wirtschaftsdünger", "EUR/a",
			dyn.revenuesManure, stat.revenuesManure));
		items.add(Item.of("Erlöse Biomethan aus Nicht-Wirtschaftsdünger",
			"EUR/a", dyn.revenuesNonManure, stat.revenuesNonManure));
		items.add(Item.of("Gesamterlöse", "EUR/a",
			dyn.totalRevenues, stat.totalRevenues).bold());
		items.add(new Item());

		items.add(Item.of("Jahresüberschuss", "EUR/a",
			dyn.annualSurplus, stat.annualSurplus).bold());
		return items;
	}

	// endregion

	private static class InvestmentLabel extends LabelProvider
		implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			if (col != 0 || !(obj instanceof PlantInvestment pi))
				return null;
			return pi.included
				? Icon.CHECKBOX_CHECKED_16.img()
				: Icon.CHECKBOX_UNCHECKED_16.img();
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof PlantInvestment pi))
				return null;
			var item = pi.item;
			return switch (col) {
				case 1 -> Labels.get(item.productType());
				case 2 -> item.asset();
				case 3 -> Num.intStr(item.initialInvestment()) + " EUR";
				case 4 -> item.lifetime() + " a";
				default -> null;
			};
		}
	}

	/// A row of the result tables: the label and two value columns, which are
	/// either the value and the unit or the dynamic and the static value.
	private static class Item {

		String label;
		String value;
		String unit;
		boolean bold;

		static Item of(String label, String value, String unit) {
			var item = new Item();
			item.label = label;
			item.value = value;
			item.unit = unit;
			return item;
		}

		static Item of(String label, String unit, double dynamic, double steady) {
			return of(label,
				Num.intStr(Math.round(dynamic)) + " " + unit,
				Num.intStr(Math.round(steady)) + " " + unit);
		}

		static Item of(
			String label, String unit, double dynamic, double steady,
			int decimalPlaces
		) {
			return of(label,
				Num.str(dynamic, decimalPlaces) + " " + unit,
				Num.str(steady, decimalPlaces) + " " + unit);
		}

		Item bold() {
			bold = true;
			return this;
		}
	}

	private static class ItemLabel extends LabelProvider
		implements ITableLabelProvider, ITableFontProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof Item item))
				return null;
			return switch (col) {
				case 0 -> item.label;
				case 1 -> item.value;
				case 2 -> item.unit;
				default -> null;
			};
		}

		@Override
		public Font getFont(Object obj, int col) {
			return obj instanceof Item item && item.bold
				? UI.boldFont()
				: null;
		}


	}

}
