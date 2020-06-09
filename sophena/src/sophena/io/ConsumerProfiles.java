package sophena.io;

import java.io.File;
import java.util.UUID;

import sophena.model.Consumer;
import sophena.model.LoadProfile;
import sophena.model.Stats;
import sophena.utils.Result;

public class ConsumerProfiles {

	private ConsumerProfiles() {
	}

	/**
	 * Reads the profile from the given file and sets it as load profile of the
	 * given consumer if there was no error. It also updates the profile related
	 * data like heating load, water fraction etc. of the consumer in this case. The
	 * result of the reading operation is returned for error checking.
	 */
	public static Result<LoadProfile> read(File file, Consumer consumer) {
		var r = LoadProfiles.read(file);
		if (r.isError())
			return r;
		consumer.profile = r.get();
		consumer.profile.id = UUID.randomUUID().toString();
		computeStats(consumer);
		return r;
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
