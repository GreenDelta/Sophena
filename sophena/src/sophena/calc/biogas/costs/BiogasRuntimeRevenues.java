package sophena.calc.biogas.costs;

import org.openlca.commons.Res;

import sophena.calc.biogas.BiogasPlants;
import sophena.calc.biogas.BiogasRuntimeResult;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.BiogasPlantSettings;
import sophena.model.biogas.ElectricityMarketValue;
import sophena.model.biogas.MarketPriceLimit;

/// The annual revenues from the electricity feed-in of a biogas plant.
///
/// The revenue calculation depends on the type of the electricity marketing
/// that is defined in the settings of the plant:
///
/// - In the fixed remuneration mode (`isFixedRemuneration`) every fed-in
///   kilowatt hour is paid with the fixed feed-in tariff (`feedInTariff`).
/// - In the market premium model a market premium is paid which is the
///   difference between the value to be applied (`marketPremiumValue`) and the
///   reference market value of the selected `ElectricityMarketValue`. The
///   premium is never negative and can be limited to hours with a certain
///   electricity price (`marketPriceLimit`) and to a maximum number of
///   eligible operating quarter hours (`eligibleQuarterHours`).
///
/// In both modes the revenues from the electricity exchange, the additional
/// revenues (Mehrerlöse) and the share that is paid to the direct marketer are
/// calculated. The fed-in electricity is the generated electricity minus the
/// cable and transformer losses (`transmissionLosses`) in every operating hour;
/// in the surplus feed-in mode the internal power demand (`avgPowerDemand`) is
/// additionally subtracted in the full-load hours.
///
/// The hourly funding and exchange revenues are in EUR, the fed-in electricity
/// and the generated heat are in kWh, and the quarter hour values are operating
/// quarter hours.
public record BiogasRuntimeRevenues(
	double[] funding,
	double fundingSum,
	double[] revenues,
	double revenuesSum,
	double directMarketerPayment,
	double feedIn,
	double totalGeneratedHeat,
	double additionalRevenues,
	double totalRevenues,
	double quarterHoursBelowZero,
	double quarterHoursBelowTwo,
	double totalQuarterHours,
	double quarterHoursAbove85
) {

	/// Calculates the revenues of the given plant for one year. The result of
	/// the runtime calculation is needed for the run flags of the plant.
	public static Res<BiogasRuntimeRevenues> calculate(
		BiogasPlant plant, BiogasRuntimeResult result
	) {
		if (plant == null)
			return Res.error("Es wurde keine Biogasanlage übergeben");
		if (result == null)
			return Res.error("Es wurde kein Berechnungsergebnis übergeben");
		var check = BiogasPlants.canCalculate(plant);
		if (check.isError())
			return Res.error(check.error());

		var settings = plant.settings;
		if (settings == null)
			return Res.error(
				"Es wurden keine Einstellungen für die Biogasanlage übergeben");
		if (!settings.isFixedRemuneration && settings.marketValue == null)
			return Res.error(
				"Es wurde kein Marktwert für das Marktprämienmodell ausgewählt");

		try {
			return Res.ok(new Calculator(plant, result).calculate());
		} catch (Exception e) {
			return Res.error("Berechnung der Stromerlöse ist fehlgeschlagen", e);
		}
	}

	private static class Calculator {

		private final BiogasPlant plant;
		private final BiogasPlantSettings settings;

		/// The run flags of the plant: a `true` value means that the plant runs
		/// under full load in that hour.
		private final boolean[] runFlags;

		/// The prices of the electricity price curve in ct/kWh.
		private final double[] prices;

		/// The electric power of the plant under full load in kW.
		private final double power;

		/// The number of ramp units in an hour: 0 = the plant is off, 1 = one
		/// ramp (up or down), 2 = two ramps (the hour is between two blocks).
		private final int[] rampUnits;

		/// The electricity that is fed into the grid in each hour in kWh.
		private final double[] feedIn;

		/// The usable heat of the plant in each hour in kWh. This is the power
		/// profile of the producer; the heat losses are already subtracted
		/// there (see `BiogasRuntimeResult`).
		private final double[] heatPower;

		Calculator(BiogasPlant plant, BiogasRuntimeResult result) {
			this.plant = plant;
			this.settings = plant.settings;
			this.runFlags = result.runFlags();
			this.prices = plant.electricityPrices != null
				? plant.electricityPrices.values
				: null;
			this.power = BiogasPlants.totalElectricPower(plant);
			this.rampUnits = new int[Stats.HOURS];
			this.feedIn = new double[Stats.HOURS];
			this.heatPower = result.producerProfile().maxPower;
			buildProfiles();
		}

		/// Creates the fed-in electricity per hour from the run flags. Full-load
		/// hours get the full electric power, ramp hours 1/8 of it per ramp. The
		/// cable and transformer losses are subtracted in every operating hour;
		/// in the surplus feed-in mode the internal power demand is additionally
		/// subtracted in the full-load hours.
		private void buildProfiles() {
			double losses = settings.transmissionLosses;
			for (int h = 0; h < Stats.HOURS; h++) {
				if (isFullLoad(h)) {
					double energy = settings.isFullFeedIn
						? power
						: power - settings.avgPowerDemand;
					feedIn[h] = Math.max(0, energy - losses);
					continue;
				}
				int ramps = 0;
				if (isFullLoad(h + 1))
					ramps++;
				if (h > 0 && isFullLoad(h - 1))
					ramps++;
				rampUnits[h] = ramps;
				feedIn[h] = ramps <= 0
					? 0
					: Math.max(0, ramps * power / 8.0 - losses);
			}
		}

		BiogasRuntimeRevenues calculate() {
			var funding = settings.isFixedRemuneration
				? calcFixedFunding()
				: calcMarketPremiumFunding();
			var revenues = calcExchangeRevenues();

			double fundingSum = Stats.sum(funding);
			double revenuesSum = Stats.sum(revenues);
			double additional = calcAdditionalRevenues(revenues);
			double directMarketer =
				additional * settings.directMarketerShare / 100.0;
			double total = fundingSum + revenuesSum - directMarketer;

			return new BiogasRuntimeRevenues(
				funding,
				fundingSum,
				revenues,
				revenuesSum,
				directMarketer,
				Stats.sum(feedIn),
				Stats.sum(heatPower),
				additional,
				total,
				quarterHoursBelowZero(),
				quarterHoursBelowTwo(),
				totalQuarterHours(),
				quarterHoursAbove85());
		}

		/// In the fixed remuneration mode every fed-in kilowatt hour that is
		/// not excluded is paid with the fixed feed-in tariff.
		private double[] calcFixedFunding() {
			var funding = new double[Stats.HOURS];
			double rate = settings.feedInTariff / 100.0;
			if (rate <= 0)
				return funding;
			for (int h = 0; h < Stats.HOURS; h++) {
				if (skipHour(h))
					continue;
				funding[h] = rate * feedIn[h];
			}
			return funding;
		}

		/// In the market premium model the premium is the difference between the
		/// value to be applied and the reference market value. It is paid for a
		/// maximum number of eligible operating quarter hours: first for all
		/// full-load hours and then for the ramp hours while there is budget
		/// left.
		private double[] calcMarketPremiumFunding() {
			var funding = new double[Stats.HOURS];
			var marketValue = settings.marketValue;
			if (marketValue == null)
				return funding;

			int budget = Math.max(0, settings.eligibleQuarterHours);

			// full-load hours first: each hour consumes 4 quarter hours
			for (int h = 0; h < Stats.HOURS && budget > 0; h++) {
				if (!isFullLoad(h) || !isFundable(h))
					continue;
				double premium = marketPremium(h, marketValue);
				if (premium <= 0)
					continue;
				int used = Math.min(4, budget);
				funding[h] = premium * feedIn[h] * (used / 4.0);
				budget -= used;
			}

			// then the ramp hours while there is budget left
			for (int h = 0; h < Stats.HOURS && budget > 0; h++) {
				int ramps = rampUnits[h];
				if (ramps <= 0 || !isFundable(h))
					continue;
				double premium = marketPremium(h, marketValue);
				if (premium <= 0)
					continue;
				int used = Math.min(ramps, budget);
				funding[h] = premium * feedIn[h] * (used / (double) ramps);
				budget -= used;
			}

			return funding;
		}

		/// The market premium of an hour in EUR/kWh. It is the difference
		/// between the value to be applied and the reference market value, but
		/// never negative.
		private double marketPremium(
			int hour, ElectricityMarketValue marketValue
		) {
			double reference = settings.useAnnualMarketValue
				? marketValue.value
				: marketValue.monthlyValueOfHour(hour);
			return Math.max(0, (settings.marketPremiumValue - reference) / 100.0);
		}

		/// The revenues from the electricity exchange in EUR: the fed-in
		/// electricity multiplied with the spot market price.
		private double[] calcExchangeRevenues() {
			var revenues = new double[Stats.HOURS];
			for (int h = 0; h < Stats.HOURS; h++) {
				if (skipHour(h))
					continue;
				revenues[h] = feedIn[h] * spotPrice(h);
			}
			return revenues;
		}

		/// The additional revenues (Mehrerlöse) in EUR: the difference between
		/// the exchange revenues and the monthly market value revenues, but
		/// never negative. The monthly market value is always used here, also
		/// when the annual value is selected for the market premium.
		private double calcAdditionalRevenues(double[] revenues) {
			var marketValue = settings.marketValue;
			if (marketValue == null)
				return 0;
			double sum = 0;
			for (int h = 0; h < Stats.HOURS; h++) {
				if (skipHour(h) || feedIn[h] <= 0)
					continue;
				double monthly =
					feedIn[h] * marketValue.monthlyValueOfHour(h) / 100.0;
				sum += Math.max(0, revenues[h] - monthly);
			}
			return sum;
		}

		/// The full-load hours multiplied with 4, in which the plant runs with
		/// an electricity price below 0 ct/kWh.
		private double quarterHoursBelowZero() {
			int count = 0;
			for (int h = 0; h < Stats.HOURS; h++) {
				if (isFullLoad(h) && spotPriceCents(h) < 0) {
					count++;
				}
			}
			return count * 4.0;
		}

		/// The full-load hours multiplied with 4, in which the plant runs with
		/// an electricity price at or below 2 ct/kWh.
		private double quarterHoursBelowTwo() {
			int count = 0;
			for (int h = 0; h < Stats.HOURS; h++) {
				if (isFullLoad(h) && spotPriceCents(h) <= 2) {
					count++;
				}
			}
			return count * 4.0;
		}

		/// The total operating quarter hours: the full-load hours multiplied
		/// with 4 plus the ramp units where each ramp is a quarter hour.
		private double totalQuarterHours() {
			double hours = 0;
			for (int h = 0; h < Stats.HOURS; h++) {
				hours += isFullLoad(h) ? 4 : rampUnits[h];
			}
			return hours;
		}

		/// The quarter hours in which the plant runs under full load; this is
		/// equivalent to at least 85 % of its power.
		private double quarterHoursAbove85() {
			int count = 0;
			for (int h = 0; h < Stats.HOURS; h++) {
				if (isFullLoad(h)) {
					count++;
				}
			}
			return count * 4.0;
		}

		/// Returns `true` when the plant runs under full load in the given hour.
		private boolean isFullLoad(int hour) {
			return runFlags != null
				&& hour >= 0
				&& hour < runFlags.length
				&& runFlags[hour];
		}

		/// Returns `true` when the funding can be paid in the given hour. The
		/// funding is blocked when the hour is excluded (see `skipHour`) or when
		/// the electricity price is below the selected price limit.
		private boolean isFundable(int hour) {
			return !skipHour(hour) && !isBlockedByPriceLimit(hour);
		}

		/// Returns `true` when the given hour is excluded from the calculation,
		/// e.g. because the feed-in is not allowed in that hour. Excluded hours
		/// get neither funding nor revenues from the electricity exchange.
		private boolean skipHour(int hour) {
			return !BiogasPlants.isFeedInAllowed(plant, hour);
		}

		private boolean isBlockedByPriceLimit(int hour) {
			var limit = settings.marketPriceLimit;
			if (limit == null || limit == MarketPriceLimit.NONE)
				return false;
			double price = spotPriceCents(hour);
			return switch (limit) {
				case BELOW_ZERO -> price < 0;
				case BELOW_TWO -> price <= 2;
				default -> false;
			};
		}

		/// The spot market price of the given hour in EUR/kWh.
		private double spotPrice(int hour) {
			return spotPriceCents(hour) / 100.0;
		}

		/// The spot market price of the given hour in ct/kWh.
		private double spotPriceCents(int hour) {
			return Stats.get(prices, hour);
		}
	}

}
