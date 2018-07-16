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

	private Project project;
	private EnergyResult energyResult;

	private CostSettings settings;
	private boolean withFunding;

	public CostCalculator(Project project, EnergyResult energyResult) {
		this.project = project;
		this.energyResult = energyResult;
		if (project == null)
			project = new Project();
		settings = project.costSettings;
		if (settings == null)
			settings = new CostSettings();
	}

	public void withFunding(boolean withFunding) {
		this.withFunding = withFunding;
	}

	public CostResult calculate() {
		CostResult r = new CostResult();
		createItems(r);
		finishCapitalCosts(r);
		addOtherCosts(r);

		// add revenues
		double revenuesElectricity = settings.electricityRevenues
				* GeneratedElectricity.getTotal(energyResult);
		r.netTotal.revenuesElectricity = Costs.annuity(project,
				revenuesElectricity, ir(),
				settings.electricityRevenuesFactor);
		r.grossTotal.revenuesElectricity = Costs.gross(project,
				r.netTotal.revenuesElectricity);

		double revenuesHeat = settings.heatRevenues * usedHeat();
		r.netTotal.revenuesHeat = Costs.annuity(project, revenuesHeat, ir(),
				settings.heatRevenuesFactor);
		r.grossTotal.revenuesHeat = Costs.gross(project,
				r.netTotal.revenuesHeat);

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
		double operationCosts = item.costs.operation * settings.hourlyWage;
		double maintenanceCosts = item.costs.investment
				* (item.costs.repair / 100 + item.costs.maintenance / 100);
		double annuityOperations = Costs.annuity(project, operationCosts,
				ir(), settings.operationFactor);
		double annuityMaintenance = Costs.annuity(project, maintenanceCosts,
				ir(), settings.maintenanceFactor);
		item.netOperationCosts = annuityOperations + annuityMaintenance;
		item.grossOperationCosts = Costs.gross(project, item.netOperationCosts);
		r.netTotal.operationCosts += item.netOperationCosts;
		r.grossTotal.operationCosts += item.grossOperationCosts;
	}

	private void addDemandCosts(CostResult r, CostResultItem item, Producer p) {
		double producedHeat = energyResult.totalHeat(p);

		// add fuel costs
		double netCosts = FuelCosts.net(project, p, energyResult);
		double grossCosts = FuelCosts.gross(project, p, energyResult);

		// add costs for electricity demand
		double netElectricityCosts = ElectricityCosts.net(producedHeat,
				settings);
		netCosts += netElectricityCosts;
		grossCosts += Costs.gross(project, netElectricityCosts);

		// add ash costs
		double netAshCosts = FuelCosts.netAshCosts(project, p, energyResult);
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
		r.netTotal.otherCosts = Costs.annuity(project, otherCosts, ir(),
				settings.operationFactor);
		r.grossTotal.otherCosts = Costs.gross(project, r.netTotal.otherCosts);
	}

	private void calcTotals(CostResult r) {
		r.netTotal.annualCosts = r.netTotal.capitalCosts
				+ r.netTotal.consumptionCosts
				+ r.netTotal.operationCosts
				+ r.netTotal.otherCosts
				- r.netTotal.revenuesElectricity
				- r.netTotal.revenuesHeat;

		// Note that there can be different VAT rates in the cost categories
		// so we have to calculate each sum separately
		r.grossTotal.annualCosts = r.grossTotal.capitalCosts
				+ r.grossTotal.consumptionCosts
				+ r.grossTotal.operationCosts
				+ r.grossTotal.otherCosts
				- r.grossTotal.revenuesElectricity
				- r.grossTotal.revenuesHeat;

		double Q = usedHeat();
		if (Q == 0) {
			r.netTotal.heatGenerationCosts = 0;
			r.grossTotal.heatGenerationCosts = 0;
		} else {
			r.netTotal.heatGenerationCosts = r.netTotal.annualCosts / Q;
			r.grossTotal.heatGenerationCosts = r.grossTotal.annualCosts / Q;
		}
	}

	/** Returns the interest rate that is used for the calculation. */
	private double ir() {
		return withFunding
				? settings.interestRateFunding
				: settings.interestRate;
	}

	/** The used heat in MWh */
	private double usedHeat() {
		double bufferLoss = Stats.sum(energyResult.bufferLoss);
		return (energyResult.totalProducedHeat
				- energyResult.heatNetLoss
				- bufferLoss) * 1 / 1000d;
	}

}
