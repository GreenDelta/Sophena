package sophena.model.biogas;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import sophena.model.BaseDataEntity;
import sophena.model.DoubleArrayConverter;
import sophena.model.Stats;

/// Market values that are used to calculate funding for electricity feed-ins.
@Entity
@Table(name = "tbl_electricity_market_values")
public class ElectricityMarketValue extends BaseDataEntity {

	/// The year for which the market value is valid.
	@Column(name = "valid_year")
	public int year;

	/// The average annual market value in ct/kWh.
	@Column(name = "value")
	public double value;

	/// The 12 monthly average values in ct/kWh: 0 = January ... 11 = December
	@Column(name = "monthly_values")
	@Convert(converter = DoubleArrayConverter.class)
	public double[] monthlyValues;

	/// The source of the values; e.g. netztransparenz.de
	@Column(name = "source")
	public String source;

	/// Returns the monthly value of the given month (1 = January ... 12 =
	/// December), or `0` when there is no value for that month.
	public double monthlyValueOf(int month) {
		if (monthlyValues == null || month < 1 || month > 12)
			return 0;
		return monthlyValues[month - 1];
	}

	@Override
	public ElectricityMarketValue copy() {
		var copy = new ElectricityMarketValue();
		copy.id = UUID.randomUUID().toString();
		copy.name = name;
		copy.description = description;
		copy.isProtected = isProtected;
		copy.year = year;
		copy.value = value;
		copy.monthlyValues = Stats.copy(monthlyValues);
		copy.source = source;
		return copy;
	}
}
