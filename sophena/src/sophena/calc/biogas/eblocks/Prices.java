package sophena.calc.biogas.eblocks;

import org.jspecify.annotations.NullMarked;

import sophena.calc.biogas.BiogasPlants;
import sophena.model.biogas.BiogasPlant;

/// The electricity prices as they are used by the block search.
@NullMarked
final class Prices {

	/// The penalty in ct/kWh that is subtracted from the price of an hour in
	/// which feed-in is not allowed.
	///
	/// The hours with a blocked feed-in are not removed from the calculation.
	/// They stay in the algorithm and can even be marked as price optimal hours
	/// when a window does not contain enough other hours, but with the penalty
	/// they are the last hours that are selected.
	static final double BLOCKED_FEED_IN_PENALTY = 1000;

	private Prices() {
	}

	/// The price value of an hour: the electricity price of that hour, reduced
	/// by the `BLOCKED_FEED_IN_PENALTY` when feed-in is not allowed in it.
	static double valueOf(BiogasPlant plant, int hour) {
		double price = plant.electricityPrices.values[hour];
		return BiogasPlants.isFeedInAllowed(plant, hour)
			? price
			: price - BLOCKED_FEED_IN_PENALTY;
	}

	/// The price of a block: the sum of the price values of its hours.
	///
	/// When blocks are compared, they always have the same number of hours (the
	/// minimum runtime of the plant), so the sum is the correct measure.
	static double sumOf(BiogasPlant plant, Block block) {
		double sum = 0;
		for (int h = block.start().hour(); h < block.end().hour(); h++) {
			sum += valueOf(plant, h);
		}
		return sum;
	}
}
