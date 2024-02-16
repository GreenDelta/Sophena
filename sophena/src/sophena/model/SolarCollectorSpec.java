package sophena.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class SolarCollectorSpec implements Copyable<SolarCollectorSpec>{
	
	@Column(name = "solar_collector_area")
	public double solarCollectorArea;
	
	@Column(name = "solar_collector_alignment")
	public double solarCollectorAlignment;
	
	@Column(name = "solar_collector_tilt")
	public double solarCollectorTilt;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "solar_collector_operating_mode")
	public SolarCollectorOperatingMode solarCollectorOperatingMode;
	
	@Column(name = "solar_collector_temperature_difference")
	public double solarCollectorTemperatureDifference;
	
	@Column(name = "solar_collector_temperature_increase")
	public double solarCollectorTemperatureIncrease;
	
	@Column(name = "solar_collector_radiation_limit")
	public double solarCollectorRadiationLimit;

	@Override
	public SolarCollectorSpec copy() {
		var clone = new SolarCollectorSpec();
		clone.solarCollectorArea = solarCollectorArea;
		clone.solarCollectorAlignment = solarCollectorAlignment;
		clone.solarCollectorTilt = solarCollectorTilt;
		clone.solarCollectorOperatingMode = solarCollectorOperatingMode;
		clone.solarCollectorTemperatureDifference = solarCollectorTemperatureDifference;
		clone.solarCollectorTemperatureIncrease = solarCollectorTemperatureIncrease;
		return clone;
	}
}
