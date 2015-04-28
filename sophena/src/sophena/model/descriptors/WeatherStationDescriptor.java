package sophena.model.descriptors;

import sophena.model.ModelType;

public class WeatherStationDescriptor extends Descriptor {

	@Override
	public ModelType getType() {
		return ModelType.WEATHER_STATION;
	}
}
