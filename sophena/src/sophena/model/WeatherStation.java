package sophena.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;

import sophena.model.descriptors.WeatherStationDescriptor;

import java.util.Arrays;
import java.util.UUID;

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
	
	@Column(name = "direction_radiation")
	@Convert("DoubleArrayConverter")
	public double[] directionRadiation;
	
	@Column(name = "diffuse_radiation")
	@Convert("DoubleArrayConverter")
	public double[] diffuseRadiation;

	@Override
	public WeatherStation copy() {
		var copy = new WeatherStation();
		copy.id = UUID.randomUUID().toString();
		copy.name = name;
		copy.description = description;
		copy.isProtected = isProtected;
		copy.longitude = longitude;
		copy.latitude = latitude;
		copy.altitude = altitude;
		copy.data = data != null
				? Arrays.copyOf(data, data.length)
				: null;
		copy.directionRadiation = directionRadiation != null
				? Arrays.copyOf(directionRadiation, directionRadiation.length)
				: null;
		copy.diffuseRadiation = diffuseRadiation != null
				? Arrays.copyOf(diffuseRadiation, diffuseRadiation.length)
				: null;
		return copy;
	}

	public WeatherStationDescriptor toDescriptor() {
		var d = new WeatherStationDescriptor();
		d.id = id;
		d.name = name;
		d.description = description;
		d.longitude = longitude;
		d.latitude = latitude;
		d.altitude = altitude;
		return d;
	}
}
