package sophena.model.biogas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import sophena.model.AbstractEntity;
import sophena.model.CostSettings;
import sophena.model.Fuel;

/// The settings of a biogas plant. This is similar to the cost settings of a
/// project: a biogas plant is always created with a settings instance.
@Entity
@Table(name = "tbl_biogas_plant_settings")
public class BiogasPlantSettings extends AbstractEntity {

	/// Average hourly wage in EUR.
	@Column(name = "hourly_wage")
	public double hourlyWage;

	/// Price for purchased electricity in EUR/kWh when operating in full
	/// feed-in mode or when the CHP unit is idle.
	@Column(name = "electricity_price")
	public double electricityPrice;

	/// The electricity mix used for self-consumption when the biogas plant
	/// is idle or when operating in full feed-in mode.
	@OneToOne
	@JoinColumn(name = "f_demand_electricity_mix")
	public Fuel demandElectricityMix;

	/// Indicates whether the plant operates in full feed-in mode. In full feed-in
	/// mode, the electricity price above is always used for self-consumption. In
	/// surplus feed-in mode, the self-consumption is subtracted from the installed
	/// capacity when the CHP is running and not fed into the grid; at other times,
	/// the electricity price above is used.
	@Column(name = "is_full_feed_in")
	public boolean isFullFeedIn;

	/// Creates a new settings instance with the default values. The demand
	/// electricity mix is taken from the given global cost settings, if it is
	/// available.
	public static BiogasPlantSettings createDefault(CostSettings global) {
		var settings = new BiogasPlantSettings();
		settings.id = UUID.randomUUID().toString();
		settings.hourlyWage = 25.0;
		settings.electricityPrice = 0.30;
		settings.isFullFeedIn = false;
		if (global != null) {
			settings.demandElectricityMix = global.electricityMix;
		}
		return settings;
	}

	@Override
	public BiogasPlantSettings copy() {
		var copy = new BiogasPlantSettings();
		copy.id = UUID.randomUUID().toString();
		copy.hourlyWage = hourlyWage;
		copy.electricityPrice = electricityPrice;
		copy.demandElectricityMix = demandElectricityMix;
		copy.isFullFeedIn = isFullFeedIn;
		return copy;
	}
}
