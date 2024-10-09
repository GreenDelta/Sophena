package sophena.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.persistence.annotations.Convert;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import sophena.model.descriptors.ProducerDescriptor;

@Entity
@Table(name = "tbl_producers")
public class Producer extends RootEntity {

	@Column(name = "is_disabled")
	public boolean disabled;

	@Column(name = "rank")
	public int rank;

	@OneToOne
	@JoinColumn(name = "f_product_group")
	public ProductGroup productGroup;

	@OneToOne
	@JoinColumn(name = "f_boiler")
	public Boiler boiler;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_profile")
	public ProducerProfile profile;

	/**
	 * This field is only relevant for producers with a producer profile (so
	 * without a boiler) and contains the maximum power of the producer. It is
	 * used for calculating the full load hours etc.
	 */
	@Column(name = "profile_max_power")
	public double profileMaxPower;

	/**
	 * If the product type of a producer is co-generation plant it is possible
	 * to include the generated electricity of that producer in the calculation.
	 */
	@Column(name = "profile_max_power_electric")
	public double profileMaxPowerElectric;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "producer_function")
	public ProducerFunction function;

	@Embedded
	public ProductCosts costs;

	@Embedded
	public FuelSpec fuelSpec;

	/**
	 * This field is only applicable for co-generation plants and contains the
	 * electricity that is produced.
	 */
	@OneToOne
	@JoinColumn(name = "f_produced_electricity")
	public Fuel producedElectricity;

	@OneToOne
	@JoinColumn(name = "f_heat_recovery")
	public HeatRecovery heatRecovery;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	public final List<TimeInterval> interruptions = new ArrayList<>();

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "investment",
					column = @Column(name = "heat_recovery_investment")),
			@AttributeOverride(name = "duration",
					column = @Column(name = "heat_recovery_duration")),
			@AttributeOverride(name = "repair",
					column = @Column(name = "heat_recovery_repair")),
			@AttributeOverride(name = "maintenance",
					column = @Column(name = "heat_recovery_maintenance")),
			@AttributeOverride(name = "operation",
					column = @Column(name = "heat_recovery_operation")) })
	public ProductCosts heatRecoveryCosts;

	/**
	 * The utlisation rate is normally calculated (field is null) but if the
	 * expert knows better it can be directly entered by the expert (field
	 * contains a value).
	 */
	@Column(name = "utilisation_rate")
	public Double utilisationRate;
	
	@OneToOne
	@JoinColumn(name = "f_solar_collector")
	public SolarCollector solarCollector;
	
	@Embedded
	public SolarCollectorSpec solarCollectorSpec;
	
	@Column(name = "is_outdoor_temperature_control")
	public boolean isOutdoorTemperatureControl;
	
	@Column(name = "outdoor_temperature")
	public double outdoorTemperature;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "outdoor_temperature_control_kind")
	public OutdoorTemperatureControlKind outdoorTemperatureControlKind;

	@OneToOne
	@JoinColumn(name = "f_heat_pump")
	public HeatPump heatPump;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "heat_pump_mode")
	public HeatPumpMode heatPumpMode;
	
	@Column(name = "source_temperature_user")
	public Double sourceTemperatureUser;
	
	@Column(name = "source_temperature_hourly")
	@Convert("DoubleArrayConverter")
	public double[] sourceTemperatureHourly;
	
	/**
	 * Indicates whether the producer is based on a producer profile or not.
	 */
	public boolean hasProfile() {
		return profile != null;
	}

	@Override
	public Producer copy() {
		var clone = new Producer();
		clone.id = UUID.randomUUID().toString();
		clone.name = name;
		clone.description = description;
		clone.disabled = disabled;
		clone.productGroup = productGroup;
		clone.boiler = boiler;
		clone.function = function;
		clone.rank = rank;
		clone.solarCollector = solarCollector;
		clone.isOutdoorTemperatureControl = isOutdoorTemperatureControl;
		clone.outdoorTemperature = outdoorTemperature;
		clone.outdoorTemperatureControlKind = outdoorTemperatureControlKind;
		if (costs != null) {
			clone.costs = costs.copy();
		}
		if (fuelSpec != null) {
			clone.fuelSpec = fuelSpec.copy();
		}
		if (solarCollectorSpec != null) {
			clone.solarCollectorSpec = solarCollectorSpec.copy();
		}
		clone.producedElectricity = producedElectricity;
		clone.heatRecovery = heatRecovery;
		if (heatRecoveryCosts != null) {
			clone.heatRecoveryCosts = heatRecoveryCosts.copy();
		}
		clone.utilisationRate = utilisationRate;
		for (var t : interruptions) {
			clone.interruptions.add(t.copy());
		}
		if (profile != null) {
			clone.profile = profile.copy();
			clone.profileMaxPower = profileMaxPower;
			clone.profileMaxPowerElectric = profileMaxPowerElectric;
		}
		return clone;
	}

	public ProducerDescriptor toDescriptor() {
		var d = new ProducerDescriptor();
		d.id = id;
		d.name = name;
		d.description = description;
		d.disabled = disabled;
		return d;
	}

}
