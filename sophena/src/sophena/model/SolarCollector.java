package sophena.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_solar_collectors")
public class SolarCollector extends AbstractProduct {

	@Column(name = "collector_area")
	public double collectorArea;

	@Column(name = "efficiency_rate_radiation")
	public double efficiencyRateRadiation;

	@Column(name = "correction_factor")
	public double correctionFactor;

	@Column(name = "heat_transfer_coefficient1")
	public double heatTransferCoefficient1;

	@Column(name = "heat_transfer_coefficient2")
	public double heatTransferCoefficient2;

	@Column(name = "heat_Capacity")
	public double heatCapacity;

	@Column(name = "angle_incidence_EW_10")
	public double angleIncidenceEW10;
	
	@Column(name = "angle_incidence_EW_20")
	public double angleIncidenceEW20;
	
	@Column(name = "angle_incidence_EW_30")
	public double angleIncidenceEW30;
	
	@Column(name = "angle_incidence_EW_40")
	public double angleIncidenceEW40;

	@Column(name = "angle_incidence_EW_50")
	public double angleIncidenceEW50;
	
	@Column(name = "angle_incidence_EW_60")
	public double angleIncidenceEW60;
	
	@Column(name = "angle_incidence_EW_70")
	public double angleIncidenceEW70;
	
	@Column(name = "angle_incidence_EW_80")
	public double angleIncidenceEW80;
	
	@Column(name = "angle_incidence_EW_90")
	public double angleIncidenceEW90;
	
	@Column(name = "angle_incidence_NS_10")
	public double angleIncidenceNS10;
	
	@Column(name = "angle_incidence_NS_20")
	public double angleIncidenceNS20;
	
	@Column(name = "angle_incidence_NS_30")
	public double angleIncidenceNS30;
	
	@Column(name = "angle_incidence_NS_40")
	public double angleIncidenceNS40;

	@Column(name = "angle_incidence_NS_50")
	public double angleIncidenceNS50;
	
	@Column(name = "angle_incidence_NS_60")
	public double angleIncidenceNS60;
	
	@Column(name = "angle_incidence_NS_70")
	public double angleIncidenceNS70;
	
	@Column(name = "angle_incidence_NS_80")
	public double angleIncidenceNS80;
	
	@Column(name = "angle_incidence_NS_90")
	public double angleIncidenceNS90;
	
	@Override
	public SolarCollector copy() {
		var clone = new SolarCollector();
		AbstractProduct.copyFields(this, clone);
		clone.id = UUID.randomUUID().toString();
		clone.collectorArea = collectorArea;
		clone.efficiencyRateRadiation = efficiencyRateRadiation;
		clone.correctionFactor = correctionFactor;
		clone.heatTransferCoefficient1 = heatTransferCoefficient1;
		clone.heatTransferCoefficient2 = heatTransferCoefficient2;
		clone.heatCapacity = heatCapacity;
		clone.angleIncidenceEW10 = angleIncidenceEW10;
		clone.angleIncidenceEW20 = angleIncidenceEW20;
		clone.angleIncidenceEW30 = angleIncidenceEW30;
		clone.angleIncidenceEW40 = angleIncidenceEW40;
		clone.angleIncidenceEW50 = angleIncidenceEW50;
		clone.angleIncidenceEW60 = angleIncidenceEW60;
		clone.angleIncidenceEW70 = angleIncidenceEW70;
		clone.angleIncidenceEW80 = angleIncidenceEW80;
		clone.angleIncidenceEW90 = angleIncidenceEW90;
		clone.angleIncidenceNS10 = angleIncidenceNS10;
		clone.angleIncidenceNS20 = angleIncidenceNS20;
		clone.angleIncidenceNS30 = angleIncidenceNS30;
		clone.angleIncidenceNS40 = angleIncidenceNS40;
		clone.angleIncidenceNS50 = angleIncidenceNS50;
		clone.angleIncidenceNS60 = angleIncidenceNS60;
		clone.angleIncidenceNS70 = angleIncidenceNS70;
		clone.angleIncidenceNS80 = angleIncidenceNS80;
		clone.angleIncidenceNS90 = angleIncidenceNS90;
		return clone;
	}
}