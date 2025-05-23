package sophena.calc;

import java.util.List;
import java.util.function.Function;

import sophena.calc.CostResult.FieldSet;
import sophena.math.costs.CapitalCosts;
import sophena.math.costs.Costs;
import sophena.math.costs.ElectricityCosts;
import sophena.math.costs.FuelCosts;
import sophena.math.costs.Fundings;
import sophena.math.costs.InvestmentCosts;
import sophena.math.energetic.GeneratedElectricity;
import sophena.model.AnnualCostEntry;
import sophena.model.CostSettings;
import sophena.model.Producer;
import sophena.model.ProductEntry;
import sophena.model.Project;
import sophena.model.Stats;

class CostCalculator {

	private final ProjectResult result;
	private final CalcLog log;
	private final Project project;

	private CostSettings settings;
	private boolean withFunding;

	public CostCalculator(ProjectResult result) {
		this.result = result;
		this.log = result.calcLog;
		this.project = result.project;
		settings = project.costSettings;
		if (settings == null) {
			settings = new CostSettings();
		}
	}

	public void withFunding(boolean withFunding) {
		this.withFunding = withFunding;
	}

	public CostResult calculate() {

		if (withFunding) {
			log.h2("Wirtschaftlichkeitsberechnung - mit Förderung");
		} else {
			log.h2("Wirtschaftlichkeitsberechnung - ohne Förderung");
		}

		CostResult r = new CostResult();
		createItems(r);
		finishCapitalCosts(r);
		addOtherCosts(r);
		addRevenues(r);
		calcTotals(r.dynamicTotal, true);
		calcTotals(r.staticTotal, false);
		return r;
	}

	private void createItems(CostResult r) {
		for (Producer producer : project.producers) {
			if (producer.disabled)
				continue;
			CostResultItem item = CostResultItem.create(producer);
			handleItem(r, item);
			addDemandCosts(r, item, producer);
		}
		for (ProductEntry entry : project.productEntries) {
			CostResultItem item = CostResultItem.create(entry);
			handleItem(r, item);
		}
		handleItems(r, CostResultItem::forTransferStations);
		handleItems(r, CostResultItem::forHeatRecoveries);
		handleItems(r, CostResultItem::forFlueGasCleanings);
		handleItem(r, CostResultItem.forBuffer(project));
		handleItems(r, CostResultItem::forPipes);
	}

	private void handleItems(CostResult r,
			Function<Project, List<CostResultItem>> generator) {
		for (CostResultItem item : generator.apply(project)) {
			handleItem(r, item);
		}
	}

	private void handleItem(CostResult r, CostResultItem item) {
		if (item == null || item.costs == null)
			return;
		r.items.add(item);
		item.investmentCosts = InvestmentCosts.get(item);
		r.dynamicTotal.investments += item.investmentCosts;
		r.staticTotal.investments += item.investmentCosts;

		// add capital costs
		log.println("=> Kapitalkosten: " + item.label);
		item.capitalCosts = CapitalCosts.get(item, project, ir(),
				settings.investmentFactor);
		log.value("Dynamisch", item.capitalCosts, "EUR/a");
		r.dynamicTotal.capitalCosts += item.capitalCosts;
		double staticCapitalCosts = CapitalCosts.get(
				item, project, ir(), 1.0);
		log.value("Statisch", staticCapitalCosts, "EUR/a");
		r.staticTotal.capitalCosts += staticCapitalCosts;
		log.println();

		// add operation costs = operation + maintenance
		log.println("=> Betriebskosten: " + item.label);
		double operationCosts = item.costs.operation * settings.hourlyWage;
		log.println("dynamisch:");
		double annuityOperations = Costs.annuity(result, operationCosts,
				ir(), settings.operationFactor);
		log.println("statisch:");
		double staticAnnuityOperations = Costs.annuity(result, operationCosts,
				ir(), 1.0);
		log.println();

		log.println("=> Instandhaltungskosten: " + item.label);
		double maintenanceCosts = item.costs.investment
				* (item.costs.repair / 100 + item.costs.maintenance / 100);
		log.println("dynamisch:");
		double annuityMaintenance = Costs.annuity(result, maintenanceCosts,
				ir(), settings.maintenanceFactor);
		log.println("statisch:");
		double staticAnnuityMaintenance = Costs.annuity(result,
				maintenanceCosts, ir(), 1.0);
		log.println();

		item.operationRelatedCosts = annuityOperations + annuityMaintenance;
		r.dynamicTotal.operationCosts += item.operationRelatedCosts;

		r.staticTotal.operationCosts += staticAnnuityOperations
				+ staticAnnuityMaintenance;
	}

	private void addDemandCosts(CostResult r, CostResultItem item, Producer p) {
		log.h3("Bedarfsgebundene Kosten: " + p.name);

		EnergyResult energyResult = result.energyResult;
		double producedHeat = energyResult.totalHeat(p);

		double fuelCosts = FuelCosts.get(result, p);
		double electricityCosts = ElectricityCosts.net(producedHeat, settings);
		double ashCosts = FuelCosts.getAshCosts(result, p);
		double costs = fuelCosts + electricityCosts + ashCosts;

		double a = Costs.annuityFactor(project, ir());
		double priceChangeFactor = FuelCosts.getPriceChangeFactor(p, settings);
		double bDynamic = Costs.cashValueFactor(project, ir(),
				priceChangeFactor);
		double bStatic = Costs.cashValueFactor(project, ir(), 1.0);

		item.demandRelatedCosts = costs * a * bDynamic;
		r.dynamicTotal.consumptionCosts += item.demandRelatedCosts;
		r.staticTotal.consumptionCosts += costs * a * bStatic;
	}

	/** Reduce capital costs by fundings and connection fees. */
	private void finishCapitalCosts(CostResult r) {
		double bonus = settings.connectionFees;
		if (withFunding) {
			double funding = Fundings.get(project, r, log);
			r.dynamicTotal.funding = funding;
			r.staticTotal.funding = funding;
			bonus += funding;
		}
		if (bonus <= 0)
			return;
		double a = Costs.annuityFactor(project, ir());
		r.dynamicTotal.capitalCosts -= (bonus * a);
		r.staticTotal.capitalCosts -= (bonus * a);
	}

	private void addOtherCosts(CostResult r) {
		double investmentShare = (settings.insuranceShare
				+ settings.otherShare
				+ settings.administrationShare) / 100;
		double staticCosts = investmentShare * r.staticTotal.investments;
		double dynamicCosts = investmentShare * r.dynamicTotal.investments;
		for (AnnualCostEntry e : settings.annualCosts) {
			staticCosts += e.value;
			dynamicCosts += e.value;
		}
		log.h3("Sonstige Kosten");
		log.println("dynamisch:");
		r.dynamicTotal.otherAnnualCosts = Costs.annuity(
				result, dynamicCosts, ir(), settings.operationFactor);
		log.println("statisch:");
		r.staticTotal.otherAnnualCosts = Costs.annuity(
				result, staticCosts, ir(), 1.0);
		log.println();
	}

	private void addRevenues(CostResult r) {
		log.h3("Stromerlöse");
		double pe = settings.electricityRevenues;
		log.value("pe: Mittlere Stromperlöse", pe, "EUR/kWh");
		double Egen = GeneratedElectricity.getTotal(result);
		log.value("Egen: Erzeugte Strommenge", Egen, "kWh");
		double revenuesElectricity = pe * Egen;
		log.value("A: Erlöse im ersten Jahr: A = pe * Egen",
				revenuesElectricity, "kWh");

		log.println("dynamisch:");
		r.dynamicTotal.revenuesElectricity = Costs.annuity(result,
				revenuesElectricity, ir(),
				settings.electricityRevenuesFactor);
		log.println("statisch:");
		r.staticTotal.revenuesElectricity = Costs.annuity(result,
				revenuesElectricity, ir(), 1.0);
		log.println();

		log.h3("Wärmeerlöse");
		double ph = settings.heatRevenues;
		log.value("ph: Mittlere Wärmeerlöse", ph, "EUR/kWh");
		double Qu = usedHeat();
		log.value("Qu: Genutzte Wärme", Qu, "MWh");
		double revenuesHeat = ph * Qu;
		log.value("A: Erlöse im ersten Jahr: A = ph * Qu",
				revenuesHeat, "kWh");
		log.println("dynamisch:");
		r.dynamicTotal.revenuesHeat = Costs.annuity(
				result, revenuesHeat, ir(), settings.heatRevenuesFactor);
		log.println("statisch:");
		r.staticTotal.revenuesHeat = Costs.annuity(
				result, revenuesHeat, ir(), 1.0);

		log.println();
	}

	private void calcTotals(FieldSet costs, boolean dynamic) {
		log.h3("Jahresüberschuss - " + (dynamic ? "dynamisch" : "statisch"));
		log.value("Wärmeerlöse", costs.revenuesHeat, "EUR/a");
		log.value("Stromerlöse", costs.revenuesElectricity, "EUR/a");
		costs.totalAnnualCosts = costs.capitalCosts
				+ costs.consumptionCosts
				+ costs.operationCosts
				+ costs.otherAnnualCosts;
		log.value("Kosten", costs.totalAnnualCosts, "EUR/a");
		costs.annualSurplus = costs.revenuesHeat
				+ costs.revenuesElectricity - costs.totalAnnualCosts;
		log.value("Jahresüberschuss: Erlöse - Kosten",
				costs.annualSurplus, "EUR/a");
		log.println();

		log.h3("Wärmegestehungskosten - "
				+ (dynamic ? "dynamisch" : "statisch"));
		double Q = usedHeat();
		log.value("Q: Genutzte Wärme", Q, "MWh/a");
		log.value("C: Jährliche Kosten", costs.totalAnnualCosts, "EUR/a");
		log.value("E: Jährliche Stromerlöse",
				costs.revenuesElectricity, "EUR/a");
		if (Q == 0) {
			costs.heatGenerationCosts = 0;
			costs.heatGenerationCosts = 0;
		} else {
			costs.heatGenerationCosts = (costs.totalAnnualCosts
					- costs.revenuesElectricity) / Q;
		}
		log.value("Wärmegestehungskosten: (C - E) / Q",
				costs.heatGenerationCosts, "EUR/MWh");
		log.println();
	}

	/** Returns the interest rate that is used for the calculation. */
	private double ir() {
		return withFunding
				? settings.interestRateFunding
				: settings.interestRate;
	}

	/** The used heat in MWh */
	private double usedHeat() {
		EnergyResult energyResult = result.energyResult;
		double bufferLoss = Stats.sum(energyResult.bufferLoss);
		return (energyResult.totalProducedHeat
				- energyResult.heatNetLoss
				- bufferLoss) * 1 / 1000d;
	}

}
