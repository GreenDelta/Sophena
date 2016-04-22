package sophena.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import sophena.model.descriptors.ConsumerDescriptor;

@Entity
@Table(name = "tbl_consumers")
public class Consumer extends RootEntity {

	@Column(name = "is_disabled")
	public boolean disabled;

	@OneToOne
	@JoinColumn(name = "f_building_state")
	public BuildingState buildingState;

	@Column(name = "demand_based")
	public boolean demandBased;

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
	@JoinColumn(name = "f_consumer")
	public List<LoadProfile> loadProfiles = new ArrayList<>();

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_location")
	public Location location;

	@OneToOne
	@JoinColumn(name = "f_transfer_station")
	public TransferStation transferStation;

	@Embedded
	public ProductCosts transferStationCosts;

	@Override
	public Consumer clone() {
		Consumer clone = new Consumer();
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
		for (FuelConsumption cons : fuelConsumptions)
			clone.fuelConsumptions.add(cons.clone());
		if (location != null)
			clone.location = location.clone();
		clone.transferStation = transferStation;
		if (transferStationCosts != null) {
			clone.transferStationCosts = transferStationCosts.clone();
		}
		return clone;
	}

	public ConsumerDescriptor toDescriptor() {
		ConsumerDescriptor d = new ConsumerDescriptor();
		d.id = id;
		d.name = name;
		d.description = description;
		d.disabled = disabled;
		return d;
	}
}
