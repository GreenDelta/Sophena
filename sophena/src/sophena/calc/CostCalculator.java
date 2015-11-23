package sophena.calc;

import sophena.math.costs.AnnuitiyFactor;
import sophena.math.costs.CapitalCosts;
import sophena.math.costs.ElectricityCosts;
import sophena.math.costs.FuelCosts;
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
	private double projectDuration;
	private boolean withFunding;

	public CostCalculator(Project project, EnergyResult energyResult) {
		this.project = project;
		this.energyResult = energyResult;
		if (project == null)
			project = new Project();
		settings = project.costSettings;
		if (settings == null)
			settings = new CostSettings();
		projectDuration = project.projectDuration;
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
		if (withFunding)
			setCapitalCostsFunding(r);
		addOtherCosts(r);
		r.netTotal.revenues = settings.electricityRevenues;
		r.grossTotal.revenues = settings.electricityRevenues * vat();
		calcTotals(r);
		return r;
	}

	private void handleNetItems(CostResult r) {
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
		double interestRate = withFunding ? settings.interestRateFunding
				: settings.interestRate;
		double af = AnnuitiyFactor.get(project, interestRate);
		double opFactor = getCashValueFactor(settings.operationFactor);
		double maFactor = getCashValueFactor(settings.maintenanceFactor);
		double opNetto = item.costs.operation * settings.hourlyWage * af * opFactor
				+ item.costs.investment
						* (item.costs.repair / 100 + item.costs.maintenance / 100)
						* af * maFactor;
		item.netOperationCosts = opNetto;
		item.grossOperationCosts = vat() * opNetto;
		r.netTotal.operationCosts += opNetto;
		r.grossTotal.operationCosts += vat() * opNetto;
	}

	private void addDemandCosts(CostResult r, CostResultItem item, Producer p) {
		double interestRate = withFunding ? settings.interestRateFunding
				: settings.interestRate;
		double af = AnnuitiyFactor.get(project, interestRate);
		double priceChangeFactor = FuelCosts.getPriceChangeFactor(p, settings);
		double cashValueFactor = getCashValueFactor(priceChangeFactor);
		double producedHeat = energyResult.totalHeat(p);
		double netCosts = FuelCosts.net(p, producedHeat)
				+ ElectricityCosts.net(producedHeat, settings);
		double grossCosts = FuelCosts.gross(p, producedHeat)
				+ ElectricityCosts.gross(producedHeat, settings);
		item.netConsumtionCosts = netCosts * cashValueFactor * af;
		item.grossConsumptionCosts = grossCosts * cashValueFactor * af;
		r.netTotal.consumptionCosts += item.netConsumtionCosts;
		r.grossTotal.consumptionCosts += item.grossConsumptionCosts;
	}

	private void setCapitalCostsFunding(CostResult r) {
		double ir = ir();
		r.netTotal.capitalCosts = r.netTotal.capitalCosts - settings.funding
				* (Math.pow(ir, projectDuration) * (ir - 1)
						/ (Math.pow(ir, projectDuration) - 1));
		r.grossTotal.capitalCosts += vat() * r.netTotal.capitalCosts;
	}

	private void addOtherCosts(CostResult r) {
		double interestRate = withFunding ? settings.interestRateFunding
				: settings.interestRate;
		double annuityFactor = AnnuitiyFactor.get(project, interestRate);
		double cashValueFactor = getCashValueFactor(settings.operationFactor);
		double share = (settings.insuranceShare
				+ settings.otherShare
				+ settings.administrationShare) / 100;
		r.netTotal.otherCosts = share * r.netTotal.investments * annuityFactor
				* cashValueFactor;
		r.grossTotal.otherCosts = r.netTotal.otherCosts * vat();
	}

	double getCashValueFactor(double priceChangeFactor) {
		double ir = ir();
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
		r.grossTotal.annualCosts = vat() * r.netTotal.annualCosts;
		r.netTotal.heatGenerationCosts = r.netTotal.annualCosts
				/ energyResult.totalProducedHeat;
		r.grossTotal.heatGenerationCosts = vat() * r.netTotal.heatGenerationCosts;
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
