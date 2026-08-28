package sophena.calc.biogas.fermentersim;

import sophena.model.Stats;
import sophena.model.WeatherStation;

record WeatherScale(
	WeatherStation station, double minTemp, double maxTemp
) {

	static WeatherScale of(WeatherStation station) {
		return new WeatherScale(
			station, Stats.min(station.data), Stats.max(station.data));
	}

}
