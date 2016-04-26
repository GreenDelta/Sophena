package sophena.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_flue_gas_cleaning")
public class FlueGasCleaning extends AbstractProduct {

	@Column(name = "flue_gas_cleaning_type")
	public String flueGasCleaningType;

	@Column(name = "max_volume_flow")
	public double maxVolumeFlow;

	@OneToOne
	@JoinColumn(name = "f_fuel")
	public Fuel fuel;

	@Column(name = "max_producer_power")
	public double maxProducerPower;

	@Column(name = "max_electricity_consumption")
	public double maxElectricityConsumption;

	@Column(name = "cleaning_method")
	public String cleaningMethod;

	@Column(name = "cleaning_type")
	public String cleaningType;

	@Column(name = "separation_efficiency")
	public double separationEfficiency;

}