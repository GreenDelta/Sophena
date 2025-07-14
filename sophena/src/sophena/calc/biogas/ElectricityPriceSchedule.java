package sophena.calc.biogas;

import java.util.ArrayList;
import java.util.Arrays;

import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;

public record ElectricityPriceSchedule(boolean[] flags) {

	public boolean shouldRunAt(int hour) {
		return flags[hour];
	}

	static ElectricityPriceSchedule calculate(
			BiogasPlant plant, BiogasProfile profile
	) {

		var flags = new boolean[Stats.HOURS];
		var schedule = new ElectricityPriceSchedule(flags);
		if (plant.electricityPrices == null
				|| plant.electricityPrices.values == null)
			return schedule;

		var storage = new BiogasStorage(
				Stats.sum(profile.volume()), plant.product);
		if (storage.size() == 0)
			return schedule;

		var prices = plant.electricityPrices.values;
		var sortSeq = new ArrayList<SeqVal>(24);
		for (int day = 0; day < 365; day++) {

			int offset = day * 24;
			int end = offset + 24;

			// fill the storage with the next 24-hour values
			storage.setEmpty();
			sortSeq.clear();
			for (int h = offset; h < end; h++) {
				storage.add(profile, h);
				sortSeq.add(new SeqVal(h, prices[h]));
			}

			int hs = (int) Math.floor(storage.hoursToEmpty());
			if ( hs > 23) {
				Arrays.fill(flags, offset, end, true);
				continue;
			}
			if (hs < 1) {
				continue;
			}

			sortSeq.sort((v1, v2) -> Double.compare(v2.value, v1.value));
			for (int i = 0; i < hs; i++) {
				int hour = sortSeq.get(i).hour;
				flags[hour] = true;
			}
		}

		return schedule;
	}

	private record SeqVal(int hour, double value) {
	}
}
