package sophena.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * A producer profile provides the minimum and maximum power of a producer for
 * each hour in a year.
 */
@Entity
@Table(name = "tbl_producer_profiles")
public class ProducerProfile extends AbstractEntity {

	@Column(name = "min_power")
	@Convert(converter = DoubleArrayConverter.class)
	public double[] minPower;

	@Column(name = "max_power")
	@Convert(converter = DoubleArrayConverter.class)
	public double[] maxPower;

	@Column(name = "temperatur_level")
	@Convert(converter = DoubleArrayConverter.class)
	public double[] temperaturLevel;

	public static ProducerProfile initEmpty() {
		ProducerProfile p = new ProducerProfile();
		p.maxPower = new double[Stats.HOURS];
		p.minPower = new double[Stats.HOURS];
		p.temperaturLevel = new double[Stats.HOURS];
		return p;
	}

	@Override
	public ProducerProfile copy() {
		var clone = new ProducerProfile();
		clone.id = UUID.randomUUID().toString();
		clone.minPower = Stats.copy(minPower);
		clone.maxPower = Stats.copy(maxPower);
		clone.temperaturLevel = Stats.copy(temperaturLevel);
		return clone;
	}
}
