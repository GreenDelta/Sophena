package sophena.model.biogas;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import sophena.model.DoubleArrayConverter;
import sophena.model.RootEntity;
import sophena.model.Stats;

@Entity
@Table(name = "tbl_electricity_price_curves")
public class ElectricityPriceCurve extends RootEntity {

	/// hourly electricity prices in ct/kWh (8760 values)
	@Column(name = "values")
	@Convert(converter = DoubleArrayConverter.class)
	public double[] values;

	@Override
	public ElectricityPriceCurve copy() {
		var clone = new ElectricityPriceCurve();
		clone.id = UUID.randomUUID().toString();
		clone.name = name;
		clone.description = description;
		clone.values = Stats.copy(values);
		return clone;
	}
}
