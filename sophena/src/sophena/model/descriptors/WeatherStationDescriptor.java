package sophena.model.descriptors;

import sophena.model.ModelType;

public class WeatherStationDescriptor extends Descriptor {

	public double longitude;
	public double latitude;
	public double altitude;

	@Override
	public ModelType getType() {
		return ModelType.WEATHER_STATION;
	}
}
