package sophena.model;

import java.nio.ByteBuffer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "tbl_weather_stations")
public class WeatherStation extends RootEntity {

	@Lob
	@Column(name = "data")
	private byte[] bytes;

	@Transient
	private double[] data;

	public double[] getData() {
		if (data != null)
			return data;
		if (bytes == null)
			return null;
		data = new double[bytes.length / 8];
		for (int i = 0; i < bytes.length; i += 8) {
			double d = ByteBuffer.wrap(bytes, i, 8).getDouble();
			data[i / 8] = d;
		}
		return data;
	}

	public void setData(double[] data) {
		this.data = data;
		bytes = new byte[data.length * 8];
		for (int i = 0; i < data.length; i++) {
			ByteBuffer.wrap(bytes, i * 8, 8).putDouble(data[i]);
		}
	}

}
