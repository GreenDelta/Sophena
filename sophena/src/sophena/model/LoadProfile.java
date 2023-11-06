package sophena.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;

/**
 * Describes the load profile of a consumer. A load profile can have a part that
 * is temperature dependent (dynamic) and a part that is temperature independent
 * (static).
 */
@Entity
@Table(name = "tbl_load_profiles")
@Converter(name = "DoubleArrayConverter",
		converterClass = DoubleArrayConverter.class)
public class LoadProfile extends AbstractEntity {

	@Column(name = "dynamic_data")
	@Convert("DoubleArrayConverter")
	public double[] dynamicData;

	@Column(name = "static_data")
	@Convert("DoubleArrayConverter")
	public double[] staticData;

	/**
	 * Initializes a new empty load profile.
	 */
	public static LoadProfile initEmpty() {
		LoadProfile p = new LoadProfile();
		p.dynamicData = new double[Stats.HOURS];
		p.staticData = new double[Stats.HOURS];
		return p;
	}

	/**
	 * Calculates the sum of the dynamic and static data of the load profile.
	 */
	public double[] calculateTotal() {
		double[] total = new double[Stats.HOURS];
		for (int i = 0; i < Stats.HOURS; i++) {
			total[i] = Stats.get(dynamicData, i) + Stats.get(staticData, i);
		}
		return total;
	}

	@Override
	public LoadProfile copy() {
		var clone = new LoadProfile();
		clone.id = UUID.randomUUID().toString();
		clone.dynamicData = Stats.copy(dynamicData);
		clone.staticData = Stats.copy(staticData);
		return clone;
	}

}
