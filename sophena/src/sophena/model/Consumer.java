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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import sophena.model.descriptors.ConsumerDescriptor;

@Entity
@Table(name = "tbl_consumers")
public class Consumer extends RootEntity {

	/**
	 * If a consumer is disabled it is excluded from the calculations of a
	 * project result.
	 */
	@Column(name = "is_disabled")
	public boolean disabled;

	@Column(name = "demand_based")
	public boolean demandBased;

	@OneToOne
	@JoinColumn(name = "f_building_state")
	public BuildingState buildingState;

	@Column(name = "heating_load")
	public double heatingLoad;

	@Column(name = "water_fraction")
	public double waterFraction;

	@Column(name = "load_hours")
	public int loadHours;

	@Column(name = "heating_limit")
	public double heatingLimit;

	@Column(name = "floor_space")
	public double floorSpace;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_consumer")
	public final List<FuelConsumption> fuelConsumptions = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	public final List<TimeInterval> interruptions = new ArrayList<>();

	/**
	 * The load profile of a consumer. If a consumer is based on a load profile
	 * it needs to be tagged as `hasProfile`.
	 */
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_profile")
	public LoadProfile profile;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_location")
	public Location location;

	@OneToOne
	@JoinColumn(name = "f_transfer_station")
	public TransferStation transferStation;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "investment",
					column = @Column(name = "transfer_station_investment")),
			@AttributeOverride(name = "duration",
					column = @Column(name = "transfer_station_duration")),
			@AttributeOverride(name = "repair",
					column = @Column(name = "transfer_station_repair")),
			@AttributeOverride(name = "maintenance",
					column = @Column(name = "transfer_station_maintenance")),
			@AttributeOverride(name = "operation",
					column = @Column(name = "transfer_station_operation")) })
	public ProductCosts transferStationCosts;

	/**
	 * Indicates whether the consumer is based on a consumer profile or not.
	 */
	public boolean hasProfile() {
		return profile != null;
	}

	@Override
	public Consumer copy() {
		var clone = new Consumer();
		clone.id = UUID.randomUUID().toString();
		clone.name = name;
		clone.description = description;
		clone.disabled = disabled;
		clone.buildingState = buildingState;
		clone.demandBased = demandBased;
		clone.heatingLimit = heatingLimit;
		clone.heatingLoad = heatingLoad;
		clone.waterFraction = waterFraction;
		clone.loadHours = loadHours;
		for (FuelConsumption cons : fuelConsumptions) {
			clone.fuelConsumptions.add(cons.copy());
		}
		for (TimeInterval i : interruptions) {
			clone.interruptions.add(i.copy());
		}
		if (profile != null) {
			clone.profile = profile.copy();
		}
		if (location != null)
			clone.location = location.copy();
		clone.transferStation = transferStation;
		if (transferStationCosts != null) {
			clone.transferStationCosts = transferStationCosts.copy();
		}
		return clone;
	}

	public ConsumerDescriptor toDescriptor() {
		var d = new ConsumerDescriptor();
		d.id = id;
		d.name = name;
		d.description = description;
		d.disabled = disabled;
		return d;
	}
}
