package sophena.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_consumers")
public class Consumer extends Facility {

	@OneToOne
	@JoinColumn(name = "f_building_state")
	private BuildingState buildingState;

	@OneToOne
	@JoinColumn(name = "f_building_type")
	private BuildingType buildingType;

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
}
