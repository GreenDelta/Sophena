package sophena.calc.biogas.kpi;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import sophena.calc.biogas.BiogasResult;
import sophena.calc.biogas.SubstrateInput;

@NullMarked
public class BiogasEmissionResult {

	/// The Carnot efficiency for heat that is exported for the heating of
	/// buildings at a temperature below 150 °C (423.15 K), see RED II, Annex VI,
	/// point 16.
	private static final double CARNOT_EFFICIENCY_HEAT = 0.3546;

	/// Returns the emissions of the biogas plant in kg CO2 eq. that are
	/// allocated to the useful heat according to RED II, Annex VI, point 16 (see
	/// https://eur-lex.europa.eu/legal-content/EN/TXT/PDF/?uri=CELEX:32018L2001
	/// page L 328/151):
	///
	/// ```
	/// E_h = E * C_h * eta_h / (C_el * eta_el + C_h * eta_h)
	/// ```
	///
	/// The thermal efficiency `eta_h` is defined there as the annual _useful_
	/// heat divided by the annual fuel input, and the electric efficiency
	/// `eta_el` as the annual electricity produced divided by the annual fuel
	/// input. With `C_el = 1`, the fuel input cancels out and we get:
	///
	/// ```
	/// E_h = E * C_h * Q_useful / (W_el + C_h * Q_useful)
	/// ```
	///
	/// Thus, heat that is produced but not used (e.g. heat that is dissipated
	/// by an emergency cooler or that is used to heat the fermenters) does not
	/// get emissions allocated.
	///
	/// @param result the result of the biogas plant
	/// @param usefulHeat the annual useful heat in kWh that is taken by the heat
	///        network from the plant, without the heat for the fermenters
	public static double getAllocatedToHeat(
		@Nullable BiogasResult result, double usefulHeat
	) {
		if (result == null || usefulHeat <= 0)
			return 0;
		var input = SubstrateInput.of(result.plant());
		double totalEmissions = input.fuelEnergy() * input.co2Factor() / 1000;
		if (totalEmissions <= 0)
			return 0;
		double electricity = Math.max(0, result.runtime().generatedElectricity());
		double heatExergy = CARNOT_EFFICIENCY_HEAT * usefulHeat;
		return totalEmissions * heatExergy / (electricity + heatExergy);
	}
}
