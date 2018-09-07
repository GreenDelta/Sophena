package sophena.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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
	 * without a boiler) and contains the maximum power of the producer for
	 * calculating full load hours etc.
	 */
	@Column(name = "profile_max_power")
	public double profileMaxPower;

	@Enumerated(EnumType.STRING)
	@Column(name = "producer_function")
	public ProducerFunction function;

	@Embedded
	public ProductCosts costs;

	@Embedded
	public FuelSpec fuelSpec;

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
		if (profile != null)
			clone.profile = profile.clone();
		clone.function = function;
		clone.rank = rank;
		if (costs != null)
			clone.costs = costs.clone();
		if (fuelSpec != null)
			clone.fuelSpec = fuelSpec.clone();
		clone.heatRecovery = heatRecovery;
		if (heatRecoveryCosts != null) {
			clone.heatRecoveryCosts = heatRecoveryCosts.clone();
		}
		clone.utilisationRate = utilisationRate;
		for (TimeInterval t : interruptions) {
			clone.interruptions.add(t.clone());
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
