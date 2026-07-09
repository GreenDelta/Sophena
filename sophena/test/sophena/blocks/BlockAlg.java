package sophena.blocks;

import java.io.File;

import org.jspecify.annotations.Nullable;

import sophena.calc.biogas.BiogasProfile;
import sophena.calc.biogas.BiogasStorage;
import sophena.db.Database;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;

/// Describes the state of a biogas storage **before** a given hour.
record State(int hour, BiogasStorage storage, BiogasProfile gasProfile) {

	static State firstOf(BiogasPlant plant) {
		var storage = BiogasStorage.of(plant);
		var profile = BiogasProfile.of(plant);
		return new State(0, storage, profile);
	}

	/// The storage is full when the next hour of produced biogas cannot be added
	/// without running the power plant.
	boolean isFull() {
		return !storage.canAdd(gasProfile, hour);
	}

	@Nullable
	State next() {
		if (hour >= Stats.HOURS)
			return null;
		var nextStorage = storage.copy();
		nextStorage.add(gasProfile, hour);
		return new State(hour + 1, nextStorage, gasProfile);
	}

	@Nullable
	State findNextFilled() {
		if (isFull())
			return null;
		var nextStorage = storage.copy();
		for (int h = hour; h < Stats.HOURS; h++) {
			if (!nextStorage.canAdd(gasProfile, h))
				return new State(h, nextStorage, gasProfile);
			nextStorage.add(gasProfile, h);
		}
		return null;
	}
}

record Block(State start, State end) {


}


public class BlockAlg {

	static void main() {
		var dbDir = new File("build/test-workspace/database");
		try (var db = new Database(dbDir)) {
			var plant = db.getAll(BiogasPlant.class).getFirst();
			System.out.println(plant.name);

			var first = State.firstOf(plant);
			var next = first.findNextFilled();

			if (next == null) {
				System.out.println("--err1");
				return;
			}

			int window = next.hour() - first.hour();
			System.out.println(window);




		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
