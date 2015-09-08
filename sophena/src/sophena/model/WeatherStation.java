package sophena.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;

import sophena.model.descriptors.WeatherStationDescriptor;

@Entity
@Table(name = "tbl_weather_stations")
@Converter(name = "DoubleArrayConverter", converterClass = DoubleArrayConverter.class)
public class WeatherStation extends RootEntity {

	@Column(name = "data")
	@Convert("DoubleArrayConverter")
	public double[] data;

	public WeatherStationDescriptor toDescriptor() {
		WeatherStationDescriptor d = new WeatherStationDescriptor();
		d.id = id;
		d.name = name;
		d.description = description;
		return d;
	}
}
