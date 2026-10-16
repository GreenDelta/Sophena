package sophena.calc.biogas;

import org.jspecify.annotations.NullMarked;
import org.openlca.commons.Res;

import sophena.calc.biogas.costs.BiogasCostCalculator;
import sophena.calc.biogas.costs.BiogasCostResult;
import sophena.calc.biogas.costs.BiogasRuntimeRevenues;
import sophena.calc.biogas.runtime.BiogasRuntimeResult;
import sophena.model.biogas.BiogasPlant;

/// The overall result of a biogas plant calculation. It bundles the plant with
/// the performance of its CHP, the runtime result, the revenues from the
/// electricity feed-in and the cost result.
@NullMarked
public record BiogasResult(
	BiogasPlant plant,
	BiogasPlantCHP chp,
	BiogasRuntimeResult runtime,
	BiogasRuntimeRevenues revenues,
	BiogasCostResult costs
) {

	/// Calculates the overall result of the given plant. When the plant cannot
	/// be calculated or when a sub-result fails, an error with the message of
	/// the failed calculation is returned. A result is only returned when all
	/// sub-results could be calculated without an error.
	public static Res<BiogasResult> calculate(BiogasPlant plant) {
		var err = Validation.full(plant);
		if (err.isError())
			return err.castError();

		try {
			var chp = BiogasPlantCHP.of(plant);
			var runtimeRes = BiogasRuntimeResult.calculate(plant);
			if (runtimeRes.isError())
				return runtimeRes.castError();
			var runtime = runtimeRes.value();

			var revenuesRes = BiogasRuntimeRevenues.calculate(plant, runtime);
			if (revenuesRes.isError())
				return revenuesRes.castError();
			var revenues = revenuesRes.value();

			var costs = new BiogasCostCalculator(plant, runtime, revenues)
				.calculate();
			return Res.ok(
				new BiogasResult(plant, chp, runtime, revenues, costs));
		} catch (Exception e) {
			return Res.error("Berechnung der Biogasanlage ist fehlgeschlagen", e);
		}
	}
}
