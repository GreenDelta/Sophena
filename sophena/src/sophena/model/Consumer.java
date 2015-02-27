package sophena.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "tbl_consumers")
public class Consumer extends Facility {

	@OneToOne
	@JoinColumn(name = "f_building_state")
	private BuildingState buildingState;

	@OneToOne
	@JoinColumn(name = "f_building_type")
	private BuildingType buildingType;

	@Transient
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

	@Override
	public Consumer clone() {
		Consumer clone = new Consumer();
		clone.setId(UUID.randomUUID().toString());
		clone.setName(this.getName());
		clone.setDescription(this.getDescription());
		clone.setBuildingState(this.getBuildingState());
		clone.setBuildingType(this.getBuildingType());
		return clone;
	}
}
