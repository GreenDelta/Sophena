package sophena.io;

import java.io.File;
import java.util.UUID;

import sophena.model.ProducerProfile;
import sophena.utils.Result;

public class ProducerProfiles {

	public static Result<ProducerProfile> read(File file) {
		// we re-use the load profile reader here
		var load = LoadProfiles.readProducer(file);
		var msg = load.message();
		if (load.isError())
			return Result.error(msg.orElse(
					"Die Datei " + file.getName()
							+ " konnte nicht gelesen werden"));

		var profile = new ProducerProfile();
		profile.id = UUID.randomUUID().toString();
		profile.maxPower = load.get().maxPower; // 1. column
		profile.minPower = load.get().minPower;  // 2. column
		profile.temperaturLevel = load.get().temperaturLevel;
		return load.isWarning() && msg.isPresent()
				? Result.warning(profile, msg.get())
				: Result.ok(profile);
	}
}
