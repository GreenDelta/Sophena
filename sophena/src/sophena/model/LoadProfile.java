package sophena.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;

@Entity
@Table(name = "tbl_load_profiles")
@Converter(name = "DoubleArrayConverter",
		converterClass = DoubleArrayConverter.class)
public class LoadProfile extends RootEntity {

	@Column(name = "data")
	@Convert("DoubleArrayConverter")
	private double[] data;

	public double[] getData() {
		return data;
	}

	public void setData(double[] data) {
		this.data = data;
	}

}
