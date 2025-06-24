package sophena.model;

import java.util.Arrays;
import java.util.UUID;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
	
	@Column(name = "reference_longitude")
	public double referenceLongitude;

	@Column(name = "data")
	@Convert("DoubleArrayConverter")
	public double[] data;
	
	@Column(name = "direct_radiation")
	@Convert("DoubleArrayConverter")
	public double[] directRadiation;
	
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
		copy.referenceLongitude = referenceLongitude;
		copy.data = data != null
				? Arrays.copyOf(data, data.length)
				: null;
		copy.directRadiation = directRadiation != null
				? Arrays.copyOf(directRadiation, directRadiation.length)
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
		d.isProtected = isProtected;
		return d;
	}
	
	public double minTemperature()
	{
		double min = Double.MAX_VALUE;
		for (int i = 0; i < Stats.HOURS; i++)
		{
			if(data[i] < min)
				min = data[i];
		}
		return min;
	}
}
