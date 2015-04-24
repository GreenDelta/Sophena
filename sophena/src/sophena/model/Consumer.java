package sophena.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import sophena.model.descriptors.ConsumerDescriptor;

@Entity
@Table(name = "tbl_consumers")
public class Consumer extends Facility {

	@OneToOne
	@JoinColumn(name = "f_building_state")
	private BuildingState buildingState;

	@OneToOne
	@JoinColumn(name = "f_building_type")
	private BuildingType buildingType;

	@Column(name = "demand_based")
	private boolean demandBased;

	@Column(name = "heating_load")
	private double heatingLoad;

	@Column(name = "water_fraction")
	private double waterFraction;

	@Column(name = "load_hours")
	private int loadHours;

	@Column(name = "heating_limit")
	private double heatingLimit;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_consumer")
	private final List<FuelConsumption> fuelConsumptions = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_consumer")
	private List<LoadProfile> loadProfiles = new ArrayList<>();

	public BuildingState getBuildingState() {
		return buildingState;
	}

	public void setBuildingState(BuildingState buildingState) {
		this.buildingState = buildingState;
	}

	public BuildingType getBuildingType() {
		return buildingType;
	}

	public void setBuildingType(BuildingType buildingType) {
		this.buildingType = buildingType;
	}

	public List<LoadProfile> getLoadProfiles() {
		return loadProfiles;
	}

	public boolean isDemandBased() {
		return demandBased;
	}

	public void setDemandBased(boolean demandBased) {
		this.demandBased = demandBased;
	}

	public double getHeatingLoad() {
		return heatingLoad;
	}

	public void setHeatingLoad(double heatingLoad) {
		this.heatingLoad = heatingLoad;
	}

	public double getWaterFraction() {
		return waterFraction;
	}

	public void setWaterFraction(double waterFraction) {
		this.waterFraction = waterFraction;
	}

	public void setLoadHours(int loadHours) {
		this.loadHours = loadHours;
	}

	public int getLoadHours() {
		return loadHours;
	}

	public double getHeatingLimit() {
		return heatingLimit;
	}

	public void setHeatingLimit(double heatingLimit) {
		this.heatingLimit = heatingLimit;
	}

	public List<FuelConsumption> getFuelConsumptions() {
		return fuelConsumptions;
	}

	@Override
	public Consumer clone() {
		Consumer clone = new Consumer();
		clone.setId(UUID.randomUUID().toString());
		clone.setName(this.getName());
		clone.setDescription(this.getDescription());
		clone.setBuildingState(this.getBuildingState());
		clone.setBuildingType(this.getBuildingType());
		clone.setDemandBased(this.isDemandBased());
		clone.setHeatingLimit(this.getHeatingLimit());
		clone.setHeatingLoad(this.getHeatingLoad());
		clone.setWaterFraction(this.getWaterFraction());
		clone.setLoadHours(this.getLoadHours());
		for (FuelConsumption cons : this.getFuelConsumptions())
			clone.getFuelConsumptions().add(cons.clone());
		return clone;
	}

	public ConsumerDescriptor toDescriptor() {
		ConsumerDescriptor d = new ConsumerDescriptor();
		d.setId(getId());
		d.setName(getName());
		d.setDescription(getDescription());
		return d;
	}
}
