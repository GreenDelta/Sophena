package sophena.calc.biogas.biomethane;

import java.util.ArrayList;
import java.util.List;

import sophena.calc.costs.Annuity;
import sophena.calc.costs.InvestmentItem;
import sophena.calc.costs.Investments;
import sophena.model.ProductType;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.Substrate;
import sophena.model.biogas.SubstrateProfile;

/// Calculates the economic performance of upgrading the biogas of a plant to
/// biomethane and selling it instead of generating electricity and heat in a
/// CHP. The calculation is independent of the CHP calculation: the gas volume
/// is calculated from the substrates of the plant and all costs are calculated
/// with the VDI 2067 annuity method from the given input. The dynamic values
/// use the price change factors of the input, the static values a price change
/// factor of 1.
///
/// The costs contain:
///
/// - capital costs: the included plant investments, the upgrading plant and
///   the gas grid connection, reduced by the investment funding
/// - consumption costs: the substrates, the internal power demand of the plant
///   (always purchased from the grid as there is no CHP), and the running costs
///   of the upgrading (including its electricity)
/// - operation costs: repair, maintenance, and operation of the investments
/// - other costs: insurance and other charges as shares of the investments and
///   the other annual costs of the plant
public class BiomethaneCalculator {

	/// The calorific value of methane in kWh/m³ under normal conditions.
	static final double CALORIFIC_VALUE = 9.97;

	/// Substrates with a CO2 emission factor below this value in g CO2 eq./kWh
	/// count as manure (about -55 g CO2 eq./MJ).
	public static final double MANURE_CO2_LIMIT = -200;

	private final BiogasPlant plant;
	private final BiomethaneInput input;

	private BiomethaneCalculator(BiogasPlant plant, BiomethaneInput input) {
		this.plant = plant;
		this.input = input;
	}

	/// Calculates the biomethane option of the given plant with the given
	/// input.
	public static BiomethaneResult calculate(
		BiogasPlant plant, BiomethaneInput input
	) {
		return new BiomethaneCalculator(plant, input).calculate();
	}

	/// Returns true if the given CO2 emission factor in g CO2 eq./kWh is the
	/// factor of a manure substrate.
	public static boolean isManure(double co2Emissions) {
		return co2Emissions < MANURE_CO2_LIMIT;
	}

	private BiomethaneResult calculate() {
		var r = new BiomethaneResult();
		if (plant == null || input == null)
			return r;
		addGas(r);
		addCapacity(r);

		var items = investmentItems(r);
		addCapitalCosts(r, items);
		addConsumptionCosts(r);
		addOperationCosts(r, items);
		addOtherCosts(r, items);
		addRevenues(r);
		finish(r.dynamicTotal);
		finish(r.staticTotal);
		return r;
	}

	/// Calculates the biogas and methane volumes from the hourly substrate
	/// inputs. These are normal volumes without the methane slip of the CHP and
	/// without the volume correction of the gas storage.
	private void addGas(BiomethaneResult r) {
		var methanes = new ArrayList<SubstrateMethane>();
		for (var p : plant.substrateProfiles) {
			if (p == null || p.substrate == null)
				continue;
			var sub = p.substrate;
			double yield = (sub.dryMatter / 100)
				* (sub.organicDryMatter / 100)
				* sub.biogasProduction;
			double biogas = 0;
			for (int h = 0; h < Stats.HOURS; h++) {
				biogas += massAt(p, h) * yield;
			}
			double methane = biogas * sub.methaneContent / 100;
			r.biogasVolume += biogas;
			r.methaneVolume += methane;
			methanes.add(new SubstrateMethane(sub, methane));
		}
		r.rawGasEnergy = r.methaneVolume * CALORIFIC_VALUE;

		double lossFactor = Math.max(0, 1 - input.methaneLoss / 100);
		for (var m : methanes) {
			var sub = m.substrate();
			boolean manure = isManure(sub.co2Emissions);
			double price = manure ? input.manurePrice : input.nonManurePrice;
			double share = r.methaneVolume > 0
				? m.volume() / r.methaneVolume
				: 0;
			r.substrates.add(new BiomethaneResult.SubstrateShare(
				sub.name, m.volume(), share, sub.co2Emissions, manure, price));
			double sold = m.volume() * CALORIFIC_VALUE * lossFactor;
			if (manure) {
				r.soldManureEnergy += sold;
			} else {
				r.soldNonManureEnergy += sold;
			}
		}
	}

	/// The substrate mass of the given profile in the given hour in t. When no
	/// hourly values are defined, the annual mass is evenly distributed.
	private static double massAt(SubstrateProfile p, int hour) {
		return p.hourlyValues != null && p.hourlyValues.length == Stats.HOURS
			? p.hourlyValues[hour]
			: p.annualMass / Stats.HOURS;
	}


	/// The capacity of the upgrading plant is designed so that the annual
	/// biogas volume can be processed in the full load hours.
	private void addCapacity(BiomethaneResult r) {
		double hours = input.fullLoadHours > 0
			? Math.min(input.fullLoadHours, Stats.HOURS)
			: Stats.HOURS;
		r.capacity = r.biogasVolume / hours;
		r.upgradingInvestment = r.capacity * input.specificInvestment;
	}

	/// The included plant investments and the new investments of the upgrading
	/// option.
	private List<InvestmentItem> investmentItems(BiomethaneResult r) {
		var items = new ArrayList<InvestmentItem>();
		for (var pi : input.plantInvestments) {
			if (pi.included) {
				items.add(pi.item);
			}
		}
		items.add(itemOf(
			"Biogasaufbereitung", r.upgradingInvestment, input.upgrading));
		items.add(itemOf(
			"Gasnetzanschluss", input.gridInvestment, input.grid));
		return items;
	}

	private static InvestmentItem itemOf(
		String name, double investment, BiomethaneInput.InvestmentInput i
	) {
		return new InvestmentItem(
			name, ProductType.OTHER_EQUIPMENT, null,
			investment, investment, i.lifetime,
			i.repair, i.maintenance, i.operation);
	}

	private void addCapitalCosts(
		BiomethaneResult r, List<InvestmentItem> items
	) {
		for (var item : items) {
			double initial = item.initialInvestment();
			r.dynamicTotal.investments += initial;
			r.staticTotal.investments += initial;
			r.dynamicTotal.capitalCosts += Investments.capitalCosts(
				item, input.duration, input.interestRate, input.investmentFactor);
			r.staticTotal.capitalCosts += Investments.capitalCosts(
				item, input.duration, input.interestRate, 1.0);
		}

		double funding = input.funding;
		if (funding <= 0)
			return;
		double a = Annuity.factor(input.duration, input.interestRate);
		for (var s : List.of(r.dynamicTotal, r.staticTotal)) {
			s.investmentFunding = funding;
			s.capitalCosts -= funding * a;
		}
	}

	private void addConsumptionCosts(BiomethaneResult r) {
		double substrateCosts = 0;
		for (var p : plant.substrateProfiles) {
			if (p != null) {
				substrateCosts += p.annualMass * p.substrateCosts;
			}
		}
		double electricityCosts = input.avgPowerDemand
			* Stats.HOURS
			* input.electricityPrice;
		double runningCosts = input.runningCosts * r.rawGasEnergy / 1000;

		r.dynamicTotal.consumptionCosts =
			dynamic(substrateCosts, input.bioFuelFactor)
				+ dynamic(electricityCosts, input.electricityFactor)
				+ dynamic(runningCosts, input.runningCostsFactor);
		r.staticTotal.consumptionCosts = stat(substrateCosts)
			+ stat(electricityCosts)
			+ stat(runningCosts);
	}

	private void addOperationCosts(
		BiomethaneResult r, List<InvestmentItem> items
	) {
		for (var item : items) {
			double operation = Investments.operationBaseOf(
				item, input.hourlyWage);
			double maintenance = Investments.maintenanceBaseOf(item);
			r.dynamicTotal.operationCosts +=
				dynamic(operation, input.operationFactor)
					+ dynamic(maintenance, input.maintenanceFactor);
			r.staticTotal.operationCosts += stat(operation) + stat(maintenance);
		}
	}

	/// The insurance and the other charges are shares of the full investment
	/// value; the other annual costs of the plant are added.
	private void addOtherCosts(BiomethaneResult r, List<InvestmentItem> items) {
		double value = 0;
		for (var item : items) {
			value += item.investment();
		}
		double share = (input.insuranceCostsShare + input.otherCostsShare) / 100;
		double costs = value * share;
		if (plant.otherAnnualCosts != null) {
			for (var entry : plant.otherAnnualCosts) {
				if (entry != null) {
					costs += entry.value;
				}
			}
		}
		r.dynamicTotal.otherAnnualCosts = dynamic(costs, input.operationFactor);
		r.staticTotal.otherAnnualCosts = stat(costs);
	}

	private void addRevenues(BiomethaneResult r) {
		double manure = r.soldManureEnergy * input.manurePrice;
		double nonManure = r.soldNonManureEnergy * input.nonManurePrice;

		var dyn = r.dynamicTotal;
		dyn.revenuesManure = dynamic(manure, input.revenuesFactor);
		dyn.revenuesNonManure = dynamic(nonManure, input.revenuesFactor);
		dyn.totalRevenues = dyn.revenuesManure + dyn.revenuesNonManure;

		var stat = r.staticTotal;
		stat.revenuesManure = stat(manure);
		stat.revenuesNonManure = stat(nonManure);
		stat.totalRevenues = stat.revenuesManure + stat.revenuesNonManure;
	}

	private static void finish(BiomethaneResult.Summary s) {
		s.totalAnnualCosts = s.capitalCosts
			+ s.consumptionCosts
			+ s.operationCosts
			+ s.otherAnnualCosts;
		s.annualSurplus = s.totalRevenues - s.totalAnnualCosts;
	}

	private double stat(double firstYearCosts) {
		return dynamic(firstYearCosts, 1.0);
	}

	private double dynamic(double firstYearCosts, double priceChange) {
		return Annuity.ofYearlyCosts(
			input.duration, firstYearCosts, input.interestRate, priceChange);
	}

	private record SubstrateMethane(Substrate substrate, double volume) {
	}
}
