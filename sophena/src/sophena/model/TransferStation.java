package sophena.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

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
	public String type;

	/** Description of the material of the transfer station. */
	@Column(name = "material")
	public String material;

	/** Description of the water heating. */
	@Column(name = "water_heating")
	public String waterHeating;

	/** Description of the control */
	@Column(name = "control")
	public String control;
}
