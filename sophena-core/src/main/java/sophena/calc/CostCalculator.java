package sophena.calc;

import java.util.List;
import java.util.function.Function;

import sophena.math.costs.CapitalCosts;
import sophena.math.costs.Costs;
import sophena.math.costs.ElectricityCosts;
import sophena.math.costs.FuelCosts;
import sophena.math.costs.Fundings;
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
		calcTotals(r);
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
		if (item == null)
			return;
		r.items.add(item);
		r.netTotal.investments += item.costs.investment;
		r.grossTotal.investments += Costs.gross(project, item.costs.investment);

		// add capital costs
		item.netCapitalCosts = CapitalCosts.get(item, project, ir());
		item.grossCapitalCosts = Costs.gross(project, item.netCapitalCosts);
		r.netTotal.capitalCosts += item.netCapitalCosts;
		r.grossTotal.capitalCosts += item.grossCapitalCosts;

		// add operation costs = operation + maintenance
		log.println("=> Betriebskosten: " + item.label);
		double operationCosts = item.costs.operation * settings.hourlyWage;
		double annuityOperations = Costs.annuity(result, operationCosts,
				ir(), settings.operationFactor);
		log.println();

		log.println("=> Instandhaltungskosten: " + item.label);
		double maintenanceCosts = item.costs.investment
				* (item.costs.repair / 100 + item.costs.maintenance / 100);
		double annuityMaintenance = Costs.annuity(result, maintenanceCosts,
				ir(), settings.maintenanceFactor);
		log.println();

		item.netOperationCosts = annuityOperations + annuityMaintenance;
		item.grossOperationCosts = Costs.gross(project, item.netOperationCosts);
		r.netTotal.operationCosts += item.netOperationCosts;
		r.grossTotal.operationCosts += item.grossOperationCosts;
	}

	private void addDemandCosts(CostResult r, CostResultItem item, Producer p) {
		log.h3("Bedarfsgebundene Kosten: " + p.name);

		EnergyResult energyResult = result.energyResult;
		double producedHeat = energyResult.totalHeat(p);

		// add fuel costs
		double netCosts = FuelCosts.net(result, p);
		double grossCosts = FuelCosts.gross(result, p, netCosts);

		// add costs for electricity demand
		double netElectricityCosts = ElectricityCosts.net(producedHeat,
				settings);
		netCosts += netElectricityCosts;
		grossCosts += Costs.gross(project, netElectricityCosts);

		// add ash costs
		double netAshCosts = FuelCosts.netAshCosts(result, p);
		netCosts += netAshCosts;
		grossCosts += Costs.gross(project, netAshCosts);

		double priceChangeFactor = FuelCosts.getPriceChangeFactor(p, settings);

		double a = Costs.annuityFactor(project, ir());
		double b = Costs.cashValueFactor(project, ir(), priceChangeFactor);

		item.netConsumtionCosts = netCosts * a * b;
		item.grossConsumptionCosts = grossCosts * a * b;
		r.netTotal.consumptionCosts += item.netConsumtionCosts;
		r.grossTotal.consumptionCosts += item.grossConsumptionCosts;
	}

	/** Reduce capital costs by fundings and connection fees. */
	private void finishCapitalCosts(CostResult r) {
		double bonus = settings.connectionFees;
		if (withFunding) {
			double funding = Fundings.get(project);
			r.netTotal.funding = funding;
			r.grossTotal.funding = Costs.gross(project, funding);
			bonus += funding;
		}
		if (bonus <= 0)
			return;
		double a = Costs.annuityFactor(project, ir());
		r.netTotal.capitalCosts -= (bonus * a);
		r.grossTotal.capitalCosts = Costs.gross(project,
				r.netTotal.capitalCosts);
	}

	private void addOtherCosts(CostResult r) {
		double investmentShare = (settings.insuranceShare
				+ settings.otherShare
				+ settings.administrationShare) / 100;
		double otherCosts = investmentShare * r.netTotal.investments;
		for (AnnualCostEntry e : settings.annualCosts) {
			otherCosts += e.value;
		}
		log.h3("Sonstige Kosten");
		r.netTotal.otherCosts = Costs.annuity(result, otherCosts, ir(),
				settings.operationFactor);
		r.grossTotal.otherCosts = Costs.gross(project, r.netTotal.otherCosts);
		log.println();
	}

	private void addRevenues(CostResult r) {
		log.h3("Stromerlöse");
		double pe = settings.electricityRevenues;
		log.value("pe: Mittlere Stromperlöse", pe, "EUR/kWh");
		double Egen = GeneratedElectricity.getTotal(result.energyResult);
		log.value("Egen: Erzeugte Strommenge", Egen, "kWh");
		double revenuesElectricity = pe * Egen;
		log.value("A: Erlöse im ersten Jahr: A = pe * Egen",
				revenuesElectricity, "kWh");
		r.netTotal.revenuesElectricity = Costs.annuity(result,
				revenuesElectricity, ir(),
				settings.electricityRevenuesFactor);
		r.grossTotal.revenuesElectricity = Costs.gross(project,
				r.netTotal.revenuesElectricity);
		log.println();

		log.h3("Wärmeerlöse");
		double ph = settings.heatRevenues;
		log.value("ph: Mittlere Wärmeerlöse", ph, "EUR/kWh");
		double Qu = usedHeat();
		log.value("Qu: Genutzte Wärme", Qu, "MWh");
		double revenuesHeat = ph * Qu;
		log.value("A: Erlöse im ersten Jahr: A = ph * Qu",
				revenuesHeat, "kWh");
		r.netTotal.revenuesHeat = Costs.annuity(result, revenuesHeat, ir(),
				settings.heatRevenuesFactor);
		r.grossTotal.revenuesHeat = Costs.gross(project,
				r.netTotal.revenuesHeat);
		log.println();
	}

	private void calcTotals(CostResult r) {
		log.h3("Jahresüberschuss (Erlöse - Kosten)");
		log.value("Wärmeerlöse",
				r.netTotal.revenuesHeat, "EUR/a");
		log.value("Stromerlöse", r.netTotal.revenuesElectricity, "EUR/a");
		double netCosts = r.netTotal.capitalCosts
				+ r.netTotal.consumptionCosts
				+ r.netTotal.operationCosts
				+ r.netTotal.otherCosts;
		log.value("Kosten", netCosts, "EUR/a");
		r.netTotal.annualSurplus = r.netTotal.revenuesHeat
				+ r.netTotal.revenuesElectricity - netCosts;
		log.value("Jahresüberschuss", r.netTotal.annualSurplus, "EUR/a");
		log.println();

		// Note that there can be different VAT rates in the cost categories
		// so we have to calculate each sum separately
		double grossCosts = r.grossTotal.capitalCosts
				+ r.grossTotal.consumptionCosts
				+ r.grossTotal.operationCosts
				+ r.grossTotal.otherCosts;
		r.grossTotal.annualSurplus = r.grossTotal.revenuesHeat
				+ r.grossTotal.revenuesElectricity - grossCosts;

		log.h3("Wärmegestehungskosten");
		double Q = usedHeat();
		log.value("Q: Genutzte Wärme", Q, "MWh/a");
		log.value("C: Jährliche Kosten", netCosts, "EUR/a");
		log.value("E: Jährliche Stromerlöse",
				r.netTotal.revenuesElectricity, "EUR/a");
		if (Q == 0) {
			r.netTotal.heatGenerationCosts = 0;
			r.grossTotal.heatGenerationCosts = 0;
		} else {
			r.netTotal.heatGenerationCosts = (netCosts
					- r.netTotal.revenuesElectricity) / Q;
			r.grossTotal.heatGenerationCosts = (grossCosts
					- r.grossTotal.revenuesElectricity) / Q;
		}
		log.value("Wärmegestehungskosten: (C - E) / Q",
				r.netTotal.heatGenerationCosts, "EUR/MWh");
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
