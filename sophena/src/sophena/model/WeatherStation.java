package sophena.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;

import sophena.model.descriptors.WeatherStationDescriptor;

@Entity
@Table(name = "tbl_weather_stations")
@Converter(name = "DoubleArrayConverter", converterClass = DoubleArrayConverter.class)
public class WeatherStation extends BaseDataEntity {

	@Column(name = "longitude")
	public double longitude;

	@Column(name = "latitude")
	public double latitude;

	@Column(name = "altitude")
	public double altitude;

	@Column(name = "data")
	@Convert("DoubleArrayConverter")
	public double[] data;

	public WeatherStationDescriptor toDescriptor() {
		WeatherStationDescriptor d = new WeatherStationDescriptor();
		d.id = id;
		d.name = name;
		d.description = description;
		d.longitude = longitude;
		d.latitude = latitude;
		d.altitude = altitude;
		return d;
	}
}
