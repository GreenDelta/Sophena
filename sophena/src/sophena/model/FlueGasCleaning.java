package sophena.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_flue_gas_cleaning")
public class FlueGasCleaning extends AbstractProduct {

	@Column(name = "flue_gas_cleaning_type")
	public String flueGasCleaningType;

	@Column(name = "max_volume_flow")
	public double maxVolumeFlow;

	@Column(name = "fuel")
	public String fuel;

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

	@Override
	public FlueGasCleaning clone() {
		FlueGasCleaning clone = new FlueGasCleaning();
		AbstractProduct.copyFields(this, clone);
		clone.id = UUID.randomUUID().toString();
		clone.flueGasCleaningType = flueGasCleaningType;
		clone.maxVolumeFlow = maxVolumeFlow;
		clone.fuel = fuel;
		clone.maxProducerPower = maxProducerPower;
		clone.maxElectricityConsumption = maxElectricityConsumption;
		clone.cleaningMethod = cleaningMethod;
		clone.cleaningType = cleaningType;
		clone.separationEfficiency = separationEfficiency;
		return clone;
	}

}