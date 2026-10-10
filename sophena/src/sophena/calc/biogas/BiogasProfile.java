package sophena.calc.biogas;

import java.util.List;

import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.SubstrateProfile;

public record BiogasProfile(double[] volume, double[] methaneContent) {

	public static BiogasProfile empty() {
		return new BiogasProfile(
				new double[Stats.HOURS],
				new double[Stats.HOURS]);
	}

	public static BiogasProfile of(SubstrateProfile s) {
		var p = empty();
		var trace = Trace.of(s);
		if (trace.isEmpty())
			return p;
		for (int h = 0; h < Stats.HOURS; h++) {
			p.volume[h] = trace.volumeAt(h);
			p.methaneContent[h] = trace.methaneContent();
		}
		return p;
	}

	public static BiogasProfile of(BiogasPlant plant) {
		if (plant == null)
			return empty();
		var profile = BiogasProfile.of(plant.substrateProfiles);

		// The methane slip is the share of methane that is not captured or
		// converted. It is applied as a direct reduction of the produced gas
		// volume before the gas storage is simulated.
		var settings = plant.settings;
		if (settings != null && settings.methaneSlip > 0) {
			double factor = Math.max(0, 1 - settings.methaneSlip / 100.0);
			var volume = profile.volume();
			for (int h = 0; h < volume.length; h++) {
				volume[h] *= factor;
			}
		}
		return profile;
	}

	public static BiogasProfile of(List<SubstrateProfile> substrates) {
		if (substrates == null || substrates.isEmpty())
			return empty();

		var p = BiogasProfile.of(substrates.getFirst());
		for (int i = 1; i < substrates.size(); i++) {
			var trace = Trace.of(substrates.get(i));
			if (trace.isEmpty())
				continue;

			for (int h = 0; h < Stats.HOURS; h++) {
				var vol = p.volume[h];
				var met = p.methaneContent[h];

				var traceVol = trace.volumeAt(h);
				var traceMet = trace.methaneContent;

				var nextVol = vol + traceVol;
				var nextMet = (vol * met + traceVol * traceMet) / nextVol;
				p.volume[h] = nextVol;
				p.methaneContent[h] = nextMet;
			}
		}
		return p;
	}

	public double volumeAt(int h) {
		return Stats.get(volume, h);
	}

	public double methaneContentAt(int h) {
		return Stats.get(methaneContent, h);
	}

	private record Trace(
			double methaneContent,
			double volumeFactor,
			double[] substrateMass,
			boolean isEmpty
	) {

		static Trace of(SubstrateProfile p) {
			if (p == null || p.substrate == null || p.hourlyValues == null)
				return new Trace(0, 0, null, true);
			var sub = p.substrate;
			double f = (sub.dryMatter / 100)
					* (sub.organicDryMatter / 100)
					* sub.biogasProduction;
			return f <= 0
					? new Trace(0, 0, null, true)
					: new Trace(sub.methaneContent / 100, f, p.hourlyValues, false);
		}

		double volumeAt(int h) {
			if (isEmpty)
				return 0;
			return Stats.get(substrateMass, h) * volumeFactor;
		}
	}
}
