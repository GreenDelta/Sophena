package sophena.calc.biogas.biomethane;

import java.util.ArrayList;
import java.util.List;

/// The result of the economic evaluation of upgrading the biogas of a plant to
/// biomethane, see `BiomethaneCalculator`. Like the cost result of a biogas
/// plant it contains a dynamic and a static summary.
public class BiomethaneResult {

	/// The annual biogas volume under normal conditions in m³/a.
	public double biogasVolume;

	/// The annual methane volume under normal conditions in m³/a.
	public double methaneVolume;

	/// The energy content of the raw gas (the methane) in kWh/a.
	public double rawGasEnergy;

	/// The capacity of the upgrading plant in m³/h of raw gas.
	public double capacity;

	/// The investment of the upgrading plant in EUR.
	public double upgradingInvestment;

	/// The energy of the biomethane from manure that is sold in kWh/a.
	public double soldManureEnergy;

	/// The energy of the biomethane from other substrates that is sold in
	/// kWh/a.
	public double soldNonManureEnergy;

	/// The substrates of the plant with their methane production.
	public final List<SubstrateShare> substrates = new ArrayList<>();

	public final Summary dynamicTotal = new Summary();
	public final Summary staticTotal = new Summary();

	/// The total energy of the biomethane that is sold in kWh/a.
	public double soldEnergy() {
		return soldManureEnergy + soldNonManureEnergy;
	}

	/// The methane content of the biogas (between 0 and 1).
	public double methaneContent() {
		return biogasVolume > 0 ? methaneVolume / biogasVolume : 0;
	}

	/// The mixed selling price of the biomethane in EUR/kWh, weighted by the
	/// methane production of the substrates.
	public double mixedPrice() {
		double energy = 0;
		double price = 0;
		for (var s : substrates) {
			energy += s.methaneVolume();
			price += s.methaneVolume() * s.price();
		}
		return energy > 0 ? price / energy : 0;
	}

	/// The methane production of a substrate.
	///
	/// @param name the name of the substrate
	/// @param methaneVolume the produced methane in m³/a
	/// @param share the share of the substrate on the total methane production
	///   (between 0 and 1)
	/// @param co2Emissions the CO2 emission factor of the substrate in
	///   g CO2 eq./kWh
	/// @param isManure `true` when the substrate counts as manure
	/// @param price the selling price of the biomethane from the substrate in
	///   EUR/kWh
	public record SubstrateShare(
		String name,
		double methaneVolume,
		double share,
		double co2Emissions,
		boolean isManure,
		double price
	) {
	}

	public static class Summary {

		/// The total initial investment in EUR.
		public double investments;

		/// The investment funding in EUR.
		public double investmentFunding;

		/// @de Kapitalgebundene Kosten
		public double capitalCosts;

		/// @de Bedarfsgebundene Kosten
		public double consumptionCosts;

		/// @de Betriebsgebundene Kosten
		public double operationCosts;

		/// @de Sonstige Kosten
		public double otherAnnualCosts;

		/// The sum of the capital, consumption, operation, and other costs.
		public double totalAnnualCosts;

		/// The revenues from biomethane that is produced from manure.
		public double revenuesManure;

		/// The revenues from biomethane that is produced from other substrates.
		public double revenuesNonManure;

		/// The total revenues from the biomethane sales.
		public double totalRevenues;

		/// The total revenues minus the total annual costs.
		/// @de Jahresüberschuss
		public double annualSurplus;
	}
}
