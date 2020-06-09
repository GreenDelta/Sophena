package sophena.io;

import java.io.File;
import java.util.UUID;

import sophena.model.Consumer;
import sophena.model.LoadProfile;
import sophena.model.Stats;

public class ConsumerProfiles {

	private ConsumerProfiles() {
	}

	/**
	 * Reads the profile from the given file and sets it as load profile of the
	 * given consumer. It also updates the profile related data like heating
	 * load, water fraction etc. of the consumer.
	 */
	public static void read(File file, Consumer consumer) {
		if (file == null || consumer == null)
			return;
		try {
			LoadProfileReader reader = new LoadProfileReader();
			consumer.profile = reader.read(file);
			consumer.profile.id = UUID.randomUUID().toString();
			computeStats(consumer);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void computeStats(Consumer consumer) {
		LoadProfile p = consumer.profile;
		if (p == null)
			return;
		double staticHeat = Stats.sum(p.staticData);
		double dynamicHeat = Stats.sum(p.dynamicData);
		double totalHeat = staticHeat + dynamicHeat;
		consumer.heatingLoad = Stats.max(p.calculateTotal());
		if (totalHeat > 0) {
			consumer.waterFraction = Math.round(100
					* staticHeat / totalHeat);
			consumer.loadHours = (int) Math
					.round(totalHeat / consumer.heatingLoad);
		}
	}
}
