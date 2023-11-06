package sophena.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_transfer_stations")
public class TransferStation extends AbstractProduct {

	@Column(name = "building_type")
	public String buildingType;

	/** Output capacity in kW. */
	@Column(name = "output_capacity")
	public double outputCapacity;

	/** Type description */
	@Column(name = "station_type")
	public String stationType;

	/** Description of the material of the transfer station. */
	@Column(name = "material")
	public String material;

	/** Description of the water heating. */
	@Column(name = "water_heating")
	public String waterHeating;

	/** Description of the control */
	@Column(name = "control")
	public String control;

	@Override
	public TransferStation copy() {
		var clone = new TransferStation();
		AbstractProduct.copyFields(this, clone);
		clone.id = UUID.randomUUID().toString();
		clone.buildingType = buildingType;
		clone.outputCapacity = outputCapacity;
		clone.stationType = stationType;
		clone.material = material;
		clone.waterHeating = waterHeating;
		clone.control = control;
		return clone;
	}
}
