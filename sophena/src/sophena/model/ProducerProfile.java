package sophena.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;

/**
 * A producer profile provides the minimum and maximum power of a producer for
 * each hour in a year.
 */
@Entity
@Table(name = "tbl_producer_profiles")
@Converter(name = "DoubleArrayConverter", converterClass = DoubleArrayConverter.class)
public class ProducerProfile extends AbstractEntity {

	@Column(name = "min_power")
	@Convert("DoubleArrayConverter")
	public double[] minPower;

	@Column(name = "max_power")
	@Convert("DoubleArrayConverter")
	public double[] maxPower;

	@Override
	public ProducerProfile copy() {
		var clone = new ProducerProfile();
		clone.id = UUID.randomUUID().toString();
		clone.minPower = Stats.copy(minPower);
		clone.maxPower = Stats.copy(maxPower);
		return clone;
	}
}
