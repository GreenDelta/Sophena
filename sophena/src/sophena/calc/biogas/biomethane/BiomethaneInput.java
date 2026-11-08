package sophena.calc.biogas.biomethane;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NullMarked;

import sophena.calc.costs.InvestmentItem;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.BiogasPlantSettings;

/// The assumptions of the economic evaluation of upgrading the biogas of a
/// plant to biomethane and selling it instead of generating electricity and
/// heat. All values are adjustable defaults: `of(BiogasPlant)` creates an input
/// with fixed defaults for the upgrading and with the values of the plant
/// settings for the general cost parameters. The input is not stored.
@NullMarked
public class BiomethaneInput {

	/// The default full load hours of the upgrading plant in h/a.
	public static final double DEFAULT_FULL_LOAD_HOURS = 7500;

	/// The default specific investment of the upgrading plant in EUR per m³/h
	/// of raw gas capacity.
	public static final double DEFAULT_SPECIFIC_INVESTMENT = 7500;

	/// The default investment of the gas grid connection in EUR.
	public static final double DEFAULT_GRID_INVESTMENT = 250_000;

	/// The default lifetime of the upgrading plant in years.
	public static final int DEFAULT_UPGRADING_LIFETIME = 15;

	/// The default lifetime of the gas grid connection in years.
	public static final int DEFAULT_GRID_LIFETIME = 30;

	/// The default running costs of the upgrading in EUR per MWh of raw gas
	/// energy.
	public static final double DEFAULT_RUNNING_COSTS = 28;

	/// The default methane loss of the upgrading in %.
	public static final double DEFAULT_METHANE_LOSS = 1;

	/// The default price change factor of the biomethane revenues.
	public static final double DEFAULT_REVENUES_FACTOR = 1.0;

	// upgrading plant

	/// The full load hours of the upgrading plant in h/a. The capacity of the
	/// upgrading plant is designed so that it can process the annual raw gas
	/// volume in these hours.
	public double fullLoadHours = DEFAULT_FULL_LOAD_HOURS;

	/// The specific investment of the upgrading plant in EUR per m³/h of raw
	/// gas capacity.
	public double specificInvestment = DEFAULT_SPECIFIC_INVESTMENT;

	/// The lifetime and the operation related costs of the upgrading plant.
	/// The investment is the specific investment times the capacity.
	public final InvestmentInput upgrading = new InvestmentInput(
		DEFAULT_UPGRADING_LIFETIME);

	/// The investment of the gas grid connection in EUR.
	public double gridInvestment = DEFAULT_GRID_INVESTMENT;

	/// The lifetime and the operation related costs of the gas grid
	/// connection.
	public final InvestmentInput grid = new InvestmentInput(
		DEFAULT_GRID_LIFETIME);

	/// The running costs of the upgrading in EUR per MWh of raw gas energy,
	/// including the electricity of the upgrading.
	public double runningCosts = DEFAULT_RUNNING_COSTS;

	/// The share of methane in % that is lost in the upgrading process and
	/// that cannot be sold.
	public double methaneLoss = DEFAULT_METHANE_LOSS;

	/// The selling price of biomethane from manure in EUR/kWh.
	public double manurePrice;

	/// The selling price of biomethane from other substrates in EUR/kWh.
	public double nonManurePrice;

	/// The investments of the biogas plant (without the CHP units) and whether
	/// they are included in the costs of the raw gas.
	public final List<PlantInvestment> plantInvestments = new ArrayList<>();

	// general cost parameters

	/// The observation period in years.
	public int duration;

	/// The capital mixed interest rate in %.
	public double interestRate;

	/// The average hourly wage in EUR/h.
	public double hourlyWage;

	/// The price of the electricity that is purchased from the grid in EUR/kWh.
	public double electricityPrice;

	/// The average internal electricity demand of the biogas plant in kW. As
	/// there is no CHP, it is always purchased from the grid.
	public double avgPowerDemand;

	/// The insurance costs in % of the investment.
	public double insuranceCostsShare;

	/// Other charges (taxes, lease payments, etc.) in % of the investment.
	public double otherCostsShare;

	/// The investment funding in EUR.
	public double funding;

	// price change factors

	public double investmentFactor;
	public double bioFuelFactor;
	public double electricityFactor;
	public double operationFactor;
	public double maintenanceFactor;

	/// The price change factor of the running costs of the upgrading.
	public double runningCostsFactor;

	/// The price change factor of the biomethane revenues.
	public double revenuesFactor = DEFAULT_REVENUES_FACTOR;

	/// Creates the default input for the given plant.
	public static BiomethaneInput of(BiogasPlant plant) {
		var input = new BiomethaneInput();
		var s = plant.settings != null
			? plant.settings
			: BiogasPlantSettings.createDefault();
		input.manurePrice = s.manureBiomethanePrice;
		input.nonManurePrice = s.nonManureBiomethanePrice;
		input.duration = plant.duration;
		input.interestRate = s.interestRate;
		input.hourlyWage = s.hourlyWage;
		input.electricityPrice = s.electricityPrice;
		input.avgPowerDemand = s.avgPowerDemand;
		input.insuranceCostsShare = s.insuranceCostsShare;
		input.otherCostsShare = s.otherCostsShare;
		input.funding = s.funding;
		input.investmentFactor = s.investmentFactor;
		input.bioFuelFactor = s.bioFuelFactor;
		input.electricityFactor = s.electricityFactor;
		input.operationFactor = s.operationFactor;
		input.maintenanceFactor = s.maintenanceFactor;
		input.runningCostsFactor = s.electricityFactor;

		for (var entry : plant.investments) {
			var item = InvestmentItem.of(entry);
			if (item != null) {
				input.plantInvestments.add(new PlantInvestment(item));
			}
		}
		return input;
	}

	/// The lifetime and the operation related costs of a new investment of the
	/// upgrading option.
	public static class InvestmentInput {

		/// The lifetime in years.
		public int lifetime;

		/// The repair costs in % of the investment per year.
		public double repair;

		/// The maintenance and inspection costs in % of the investment per year.
		public double maintenance;

		/// The operation effort in h/a.
		public double operation;

		InvestmentInput(int lifetime) {
			this.lifetime = lifetime;
		}
	}

	/// An investment of the biogas plant. By default, all investments of the
	/// plant are included in the costs of the raw gas; only the CHP units are
	/// not part of the plant investments.
	public static class PlantInvestment {

		public final InvestmentItem item;
		public boolean included = true;

		PlantInvestment(InvestmentItem item) {
			this.item = item;
		}
	}
}
