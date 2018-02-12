package sophena.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_building_states")
public class BuildingState extends BaseDataEntity {

	@Column(name = "idx")
	public int index;

	@Column(name = "is_default")
	public boolean isDefault;

	@Enumerated(EnumType.STRING)
	@Column(name = "building_type")
	public BuildingType type;

	@Column(name = "heating_limit")
	public double heatingLimit;

	@Column(name = "antifreezing_temperature")
	public double antifreezingTemperature;

	@Column(name = "water_fraction")
	public double waterFraction;

	@Column(name = "load_hours")
	public int loadHours;

	@Override
	public BuildingState clone() {
		BuildingState clone = new BuildingState();
		clone.id = UUID.randomUUID().toString();
		clone.name = name;
		clone.description = description;
		clone.isProtected = isProtected;
		clone.index = index;
		clone.isDefault = isDefault;
		clone.type = type;
		clone.heatingLimit = heatingLimit;
		clone.antifreezingTemperature = antifreezingTemperature;
		clone.waterFraction = waterFraction;
		clone.loadHours = loadHours;
		return clone;
	}
}
