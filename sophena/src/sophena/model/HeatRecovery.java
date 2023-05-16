package sophena.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_heat_recovery")
public class HeatRecovery extends AbstractProduct {

	@Column(name = "power")
	public double power;

	@Column(name = "heat_recovery_type")
	public String heatRecoveryType;

	@Column(name = "fuel")
	public String fuel;

	@Column(name = "producer_power")
	public double producerPower;

	@Override
	public HeatRecovery clone() {
		HeatRecovery clone = new HeatRecovery();
		AbstractProduct.copyFields(this, clone);
		clone.id = UUID.randomUUID().toString();
		clone.power = power;
		clone.heatRecoveryType = heatRecoveryType;
		clone.fuel = fuel;
		clone.producerPower = producerPower;
		return clone;
	}

}
