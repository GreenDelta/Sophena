package sophena.calc.biogas.kpi;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import sophena.calc.biogas.BiogasResult;
import sophena.calc.biogas.SubstrateInput;

@NullMarked
public class BiogasEmissionResult {

	public static double getAllocatedToHeat(@Nullable BiogasResult result) {
		if (result == null)
			return 0;
		var input = SubstrateInput.of(result.plant());
		double totalEmissions = input.fuelEnergy() * input.co2Factor() / 1000;
		// allocate the emissions to heat according to RED III, see
		// https://eur-lex.europa.eu/legal-content/EN/TXT/PDF/?uri=CELEX:32018L2001
		// page L 328/151
		var chp = result.chp();
		if (!chp.isValid())
			return 0;
		// for heat under 150 °C used for heating of buildings
		double carnotEfficiency = 0.3546;
		double heatFactor = carnotEfficiency * chp.thermalEfficiency();
		return totalEmissions * heatFactor
			/ (chp.electricEfficiency() + heatFactor);
	}
}
