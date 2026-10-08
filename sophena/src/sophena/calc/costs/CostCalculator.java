package sophena.calc.costs;

import java.util.List;
import java.util.function.Function;

import sophena.calc.ProjectResult;
import sophena.calc.costs.CostResult.FieldSet;
import sophena.calc.kpi.GeneratedElectricity;
import sophena.calc.simulation.EnergyResult;
import sophena.model.AnnualCostEntry;
import sophena.model.CostSettings;
import sophena.model.Producer;
import sophena.model.ProductEntry;
import sophena.model.Project;
import sophena.model.Stats;

public class CostCalculator {

	private final ProjectResult result;
	private final Project project;

	private CostSettings settings;
	private boolean withFunding;

	public CostCalculator(ProjectResult result) {
		this.result = result;
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
		item.capitalCosts = capitalCostsOf(item, settings.investmentFactor);
		r.dynamicTotal.capitalCosts += item.capitalCosts;
		double staticCapitalCosts = capitalCostsOf(item, 1.0);
		r.staticTotal.capitalCosts += staticCapitalCosts;

		// add operation costs = operation + maintenance
		double operationCosts = item.costs.operation * settings.hourlyWage;
		double annuityOperations = Costs.annuity(result, operationCosts,
				ir(), settings.operationFactor);
		double staticAnnuityOperations = Costs.annuity(result, operationCosts,
				ir(), 1.0);

		double maintenanceCosts = Investments.maintenanceBase(
				item.investmentCosts,
				item.costs.repair,
				item.costs.maintenance);
		double annuityMaintenance = Costs.annuity(result, maintenanceCosts,
				ir(), settings.maintenanceFactor);
		double staticAnnuityMaintenance = Costs.annuity(result,
				maintenanceCosts, ir(), 1.0);

		item.operationRelatedCosts = annuityOperations + annuityMaintenance;
		r.dynamicTotal.operationCosts += item.operationRelatedCosts;

		r.staticTotal.operationCosts += staticAnnuityOperations
				+ staticAnnuityMaintenance;
	}

	/// The annual capital costs of the given item. Returns 0 when the project
	/// has no cost settings.
	private double capitalCostsOf(CostResultItem item, double priceChange) {
		if (project.costSettings == null)
			return 0;
		return Investments.capitalCosts(
				item.investmentCosts,
				item.investmentCosts,
				item.costs.duration,
				project.duration,
				ir(),
				priceChange);
	}

	private void addDemandCosts(CostResult r, CostResultItem item, Producer p) {

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
			double funding = Fundings.get(project, r);
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

		r.dynamicTotal.otherAnnualCosts = Costs.annuity(
				result, dynamicCosts, ir(), settings.operationFactor);
		r.staticTotal.otherAnnualCosts = Costs.annuity(
				result, staticCosts, ir(), 1.0);
	}

	private void addRevenues(CostResult r) {
		double pe = settings.electricityRevenues;
		double Egen = GeneratedElectricity.getTotal(result);
		double revenuesElectricity = pe * Egen;

		r.dynamicTotal.revenuesElectricity = Costs.annuity(result,
				revenuesElectricity, ir(),
				settings.electricityRevenuesFactor);
		r.staticTotal.revenuesElectricity = Costs.annuity(result,
				revenuesElectricity, ir(), 1.0);

		double ph = settings.heatRevenues;
		double Qu = usedHeat();
		double revenuesHeat = ph * Qu;
		r.dynamicTotal.revenuesHeat = Costs.annuity(
				result, revenuesHeat, ir(), settings.heatRevenuesFactor);
		r.staticTotal.revenuesHeat = Costs.annuity(
				result, revenuesHeat, ir(), 1.0);
	}

	private void calcTotals(FieldSet costs, boolean dynamic) {
		costs.totalAnnualCosts = costs.capitalCosts
				+ costs.consumptionCosts
				+ costs.operationCosts
				+ costs.otherAnnualCosts;
		costs.annualSurplus = costs.revenuesHeat
				+ costs.revenuesElectricity - costs.totalAnnualCosts;

		double Q = usedHeat();
		if (Q == 0) {
			costs.heatGenerationCosts = 0;
			costs.heatGenerationCosts = 0;
		} else {
			costs.heatGenerationCosts = (costs.totalAnnualCosts
					- costs.revenuesElectricity) / Q;
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
		EnergyResult energyResult = result.energyResult;
		double bufferLoss = Stats.sum(energyResult.bufferLoss);
		return (energyResult.totalProducedHeat
				- energyResult.heatNetLoss
				- bufferLoss) * 1 / 1000d;
	}

}
