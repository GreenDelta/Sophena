package sophena.model.biogas;

import java.time.Month;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import sophena.model.BaseDataEntity;
import sophena.model.DoubleArrayConverter;
import sophena.model.HoursTrace;
import sophena.model.Stats;

/// Market values that are used to calculate funding for electricity feed-ins.
@Entity
@Table(name = "tbl_electricity_market_values")
public class ElectricityMarketValue extends BaseDataEntity {

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

	public double valueOf(Month month) {
		return month != null
			? monthlyValueOf(month.getValue())
			: value;
	}

	/// Returns the monthly value of the given month (1 = January ... 12 =
	/// December), or the yearly average when there is no value for that month.
	public double monthlyValueOf(int month) {
		return monthlyValues == null || month < 1 || month > 12
			? value
			: monthlyValues[month - 1];
	}

	/// Returns the monthly value that is applied to the given hour of an
	/// annual hours trace (0 = 01.01. 00:00 - 01:00 ... 8759 = 31.12. 23:00 -
	/// 00:00). As in other hours traces, the February is assumed to have 28
	/// days. When no monthly values are available, the annual `value` is
	/// returned.
	public double monthlyValueOfHour(int hour) {
		if (monthlyValues == null || monthlyValues.length < 12)
			return value;
		if (hour < 0)
			return monthlyValues[0];
		if (hour >= Stats.HOURS)
			return monthlyValues[11];

		int day = hour / 24;
		int days = 0;
		for (int month = 0; month < 12; month++) {
			days += HoursTrace.DAYS_IN_MONTH[month];
			if (day < days)
				return monthlyValues[month];
		}
		return monthlyValues[11];
	}

	@Override
	public ElectricityMarketValue copy() {
		var copy = new ElectricityMarketValue();
		copy.id = UUID.randomUUID().toString();
		copy.name = name;
		copy.description = description;
		copy.isProtected = isProtected;
		copy.value = value;
		copy.monthlyValues = Stats.copy(monthlyValues);
		copy.source = source;
		return copy;
	}
}
