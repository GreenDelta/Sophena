package sophena.calc;

import sophena.math.costs.CapitalCosts;
import sophena.math.costs.Costs;
import sophena.math.costs.ElectricityCosts;
import sophena.math.costs.FuelCosts;
import sophena.math.energetic.GeneratedElectricity;
import sophena.model.CostSettings;
import sophena.model.HeatNet;
import sophena.model.HeatNetPipe;
import sophena.model.Producer;
import sophena.model.ProductEntry;
import sophena.model.Project;

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
		handleNetItems(r);
		finishCapitalCosts(r);
		addOtherCosts(r);
		r.netTotal.revenues = settings.electricityRevenues
				* GeneratedElectricity.getTotal(energyResult);
		r.grossTotal.revenues = r.netTotal.revenues * vat();
		calcTotals(r);
		return r;
	}

	private void handleNetItems(CostResult r) {
		for (CostResultItem item : CostResultItem
				.forTransferStations(project)) {
			handleItem(r, item);
		}
		for (CostResultItem item : CostResultItem.forHeatRecoveries(project)) {
			handleItem(r, item);
		}
		for (CostResultItem item : CostResultItem
				.forFlueGasCleanings(project)) {
			handleItem(r, item);
		}
		HeatNet net = project.heatNet;
		if (net == null)
			return;
		CostResultItem item = CostResultItem.createForBuffer(net);
		handleItem(r, item);
		for (HeatNetPipe pipe : net.pipes) {
			CostResultItem pipeItem = CostResultItem.create(pipe);
			handleItem(r, pipeItem);
		}
	}

	private void handleItem(CostResult r, CostResultItem item) {
		r.items.add(item);
		r.netTotal.investments += item.costs.investment;
		r.grossTotal.investments += vat() * item.costs.investment;
		addCapitalCosts(r, item);
		addOperationCosts(r, item);
	}

	private void addCapitalCosts(CostResult r, CostResultItem item) {
		double interestRate = withFunding ? settings.interestRateFunding
				: settings.interestRate;
		item.netCapitalCosts = CapitalCosts.get(item, project, interestRate);
		item.grossCapitalCosts = vat() * item.netCapitalCosts;
		r.netTotal.capitalCosts += item.netCapitalCosts;
		r.grossTotal.capitalCosts += item.grossCapitalCosts;
	}

	private void addOperationCosts(CostResult r, CostResultItem item) {
		double opFactor = getCashValueFactor(settings.operationFactor);
		double maFactor = getCashValueFactor(settings.maintenanceFactor);
		double opNetto = item.costs.operation * settings.hourlyWage
				* annuityFactor()
				* opFactor
				+ item.costs.investment
						* (item.costs.repair / 100
								+ item.costs.maintenance / 100)
						* annuityFactor() * maFactor;
		item.netOperationCosts = opNetto;
		item.grossOperationCosts = vat() * opNetto;
		r.netTotal.operationCosts += opNetto;
		r.grossTotal.operationCosts += vat() * opNetto;
	}

	private void addDemandCosts(CostResult r, CostResultItem item, Producer p) {
		double producedHeat = energyResult.totalHeat(p);

		// add fuel costs
		double netCosts = FuelCosts.net(p, energyResult);
		double grossCosts = FuelCosts.gross(p, energyResult);

		// add costs for electricity demand
		double netElectricityCosts = ElectricityCosts.net(producedHeat,
				settings);
		netCosts += netElectricityCosts;
		grossCosts += Costs.gross(netElectricityCosts, settings);

		// add ash costs
		double netAshCosts = FuelCosts.netAshCosts(p, energyResult);
		netCosts += netAshCosts;
		grossCosts += Costs.gross(netAshCosts, settings);

		double priceChangeFactor = FuelCosts.getPriceChangeFactor(p, settings);
		double cashValueFactor = getCashValueFactor(priceChangeFactor);
		item.netConsumtionCosts = netCosts * cashValueFactor * annuityFactor();
		item.grossConsumptionCosts = grossCosts * cashValueFactor
				* annuityFactor();
		r.netTotal.consumptionCosts += item.netConsumtionCosts;
		r.grossTotal.consumptionCosts += item.grossConsumptionCosts;
	}

	/** Reduce capital costs with fundings etc. */
	private void finishCapitalCosts(CostResult r) {
		if (withFunding) {
			r.netTotal.capitalCosts -= (settings.funding * annuityFactor());
			r.grossTotal.capitalCosts = vat() * r.netTotal.capitalCosts;
		}
		// TODO: connection fees
	}

	private void addOtherCosts(CostResult r) {
		double cashValueFactor = getCashValueFactor(settings.operationFactor);
		double share = (settings.insuranceShare
				+ settings.otherShare
				+ settings.administrationShare) / 100;
		r.netTotal.otherCosts = share * r.netTotal.investments * annuityFactor()
				* cashValueFactor;
		r.grossTotal.otherCosts = r.netTotal.otherCosts * vat();
	}

	double getCashValueFactor(double priceChangeFactor) {
		double ir = ir();
		double projectDuration = project.duration;
		if (ir == priceChangeFactor)
			return ((double) projectDuration) / ir;
		else
			return (1 - Math.pow(priceChangeFactor / ir, projectDuration))
					/ (ir - priceChangeFactor);
	}

	private void calcTotals(CostResult r) {
		r.netTotal.annualCosts = r.netTotal.capitalCosts
				+ r.netTotal.consumptionCosts
				+ r.netTotal.operationCosts
				+ r.netTotal.otherCosts
				- r.netTotal.revenues;

		// Note that there can be different VAT rates in the cost categories
		// so we have to calculate each sum separately
		r.grossTotal.annualCosts = r.grossTotal.capitalCosts
				+ r.grossTotal.consumptionCosts
				+ r.grossTotal.operationCosts
				+ r.grossTotal.otherCosts
				- r.grossTotal.revenues;

		double aQ = (energyResult.totalProducedHeat - energyResult.heatNetLoss);
		r.netTotal.heatGenerationCosts = r.netTotal.annualCosts / aQ;
		r.grossTotal.heatGenerationCosts = r.grossTotal.annualCosts / aQ;
	}

	private double annuityFactor() {
		double ir = withFunding ? settings.interestRateFunding
				: settings.interestRate;
		return Costs.annuityFactor(project, ir);
	}

	/** Returns the interest rate that is used for the calculation. */
	private double ir() {
		double rawRate = withFunding ? settings.interestRateFunding
				: settings.interestRate;
		return 1 + rawRate / 100;
	}

	/** Returns the VAT rate that is used for the calculation. */
	private double vat() {
		return 1 + settings.vatRate / 100;
	}

}
