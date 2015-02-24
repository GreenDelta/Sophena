package sophena.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_weather_stations")
public class WeatherStation extends RootEntity {

	@Lob
	@Column(name = "data")
	private double[] data;

	public double[] getData() {
		return data;
	}

	public void setData(double[] data) {
		this.data = data;
	}

}
