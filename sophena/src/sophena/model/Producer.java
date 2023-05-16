package sophena.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

	/**
	 * Indicates whether the producer is based on a producer profile or not.
	 */
	public boolean hasProfile() {
		return profile != null;
	}

	@Override
	public Producer clone() {
		Producer clone = new Producer();
		clone.id = UUID.randomUUID().toString();
		clone.name = name;
		clone.description = description;
		clone.disabled = disabled;
		clone.productGroup = productGroup;
		clone.boiler = boiler;
		clone.function = function;
		clone.rank = rank;
		if (costs != null)
			clone.costs = costs.clone();
		if (fuelSpec != null) {
			clone.fuelSpec = fuelSpec.clone();
		}
		clone.producedElectricity = producedElectricity;
		clone.heatRecovery = heatRecovery;
		if (heatRecoveryCosts != null) {
			clone.heatRecoveryCosts = heatRecoveryCosts.clone();
		}
		clone.utilisationRate = utilisationRate;
		for (TimeInterval t : interruptions) {
			clone.interruptions.add(t.clone());
		}
		if (profile != null) {
			clone.profile = profile.clone();
			clone.profileMaxPower = profileMaxPower;
			clone.profileMaxPowerElectric = profileMaxPowerElectric;
		}
		return clone;
	}

	public ProducerDescriptor toDescriptor() {
		ProducerDescriptor d = new ProducerDescriptor();
		d.id = id;
		d.name = name;
		d.description = description;
		d.disabled = disabled;
		return d;
	}

}
