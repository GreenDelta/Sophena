package sophena.model;

import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_heat_nets")
public class HeatNet extends AbstractEntity {

	@Column(name = "net_length")
	public double length;

	@Column(name = "supply_temperature")
	public double supplyTemperature;

	@Column(name = "return_temperature")
	public double returnTemperature;

	@Column(name = "simultaneity_factor")
	public double simultaneityFactor;

	@Column(name = "smoothing_factor")
	public Double smoothingFactor;

	@Column(name = "max_load")
	public Double maxLoad;

	@OneToOne
	@JoinColumn(name = "f_buffer_tank")
	public BufferTank bufferTank;

	@Column(name = "max_buffer_load_temperature")
	public double maxBufferLoadTemperature;

	@Column(name = "lower_buffer_load_temperature")
	public Double lowerBufferLoadTemperature;

	@Column(name = "buffer_lambda")
	public double bufferLambda;

	@Embedded
	public ProductCosts bufferTankCosts;

	/** Loss of the heating network per meter of net length in [W/m]. */
	@Column(name = "power_loss")
	public double powerLoss;

	@JoinColumn(name = "f_interruption")
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	public TimeInterval interruption;

	@JoinColumn(name = "f_heat_net")
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	public final List<HeatNetPipe> pipes = new ArrayList<>();
	
	@Column(name = "maximum_performance")
	public double maximumPerformance;
	
	@Column(name = "is_seasonal_driving_style")
	public boolean isSeasonalDrivingStyle;
	
	@JoinColumn(name = "f_interval_winter")
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	public TimeInterval intervalWinter;
	
	@JoinColumn(name = "f_interval_summer")
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	public TimeInterval intervalSummer;
	
	@Column(name = "target_charge_level_winter")
	public double targetChargeLevelWinter;
	
	@Column(name = "target_charge_level_summer")
	public double targetChargeLevelSummer;
	
	@Column(name = "flow_temperature_winter")
	public double flowTemperatureWinter;
	
	@Column(name = "flow_temperature_summer")
	public double flowTemperatureSummer;
	
	@Column(name = "return_temperature_winter")
	public double returnTemperatureWinter;
	
	@Column(name = "return_temperature_summer")
	public double returnTemperatureSummer;
	
	@Column(name = "target_charge_level")
	public double targetChargeLevel;

	public static void addDefaultTo(Project p) {
		if (p == null)
			return;
		var net = new HeatNet();
		p.heatNet = net;
		net.id = UUID.randomUUID().toString();
		net.simultaneityFactor = 1;
		net.powerLoss = 20;
		net.maxBufferLoadTemperature = 95;
		net.bufferLambda = 0.04;
		net.supplyTemperature = 80;
		net.returnTemperature = 50;

		net.isSeasonalDrivingStyle = false;
		net.targetChargeLevelWinter = 50;
		net.targetChargeLevelSummer = 0;
		net.targetChargeLevel = 50;
		net.flowTemperatureWinter = 80;
		net.flowTemperatureSummer = 80;
		net.returnTemperatureWinter = 60;
		net.returnTemperatureSummer = 60;
		TimeInterval intervalWinter = new TimeInterval();
		intervalWinter.description = "Winter";
		intervalWinter.id = UUID.randomUUID().toString();
		intervalWinter.start = MonthDay.of(11, 15).toString();
		intervalWinter.end = MonthDay.of(3, 15).toString();
		net.intervalWinter = intervalWinter;
		TimeInterval intervalSummer = new TimeInterval();
		intervalSummer.description = "Sommer";
		intervalSummer.id = UUID.randomUUID().toString();
		intervalSummer.start = MonthDay.of(5, 15).toString();
		intervalSummer.end = MonthDay.of(9, 15).toString();
		net.intervalSummer = intervalSummer;
	}
	
	@Override
	public HeatNet copy() {
		var clone = new HeatNet();
		clone.id = UUID.randomUUID().toString();
		clone.maxBufferLoadTemperature = maxBufferLoadTemperature;
		clone.lowerBufferLoadTemperature = lowerBufferLoadTemperature;
		clone.bufferLambda = bufferLambda;
		clone.bufferTank = bufferTank;
		if (bufferTankCosts != null) {
			clone.bufferTankCosts = bufferTankCosts.copy();
		}
		clone.length = length;
		clone.powerLoss = powerLoss;
		clone.returnTemperature = returnTemperature;
		clone.simultaneityFactor = simultaneityFactor;
		clone.smoothingFactor = smoothingFactor;
		clone.maxLoad = maxLoad;
		clone.supplyTemperature = supplyTemperature;
		if (interruption != null) {
			clone.interruption = interruption.copy();
		}
		clone.maximumPerformance = maximumPerformance;
		clone.isSeasonalDrivingStyle = isSeasonalDrivingStyle;
		clone.targetChargeLevelWinter = targetChargeLevelWinter;
		clone.targetChargeLevelSummer = targetChargeLevelSummer;
		clone.targetChargeLevel = targetChargeLevel;
		clone.flowTemperatureWinter = flowTemperatureWinter;
		clone.flowTemperatureSummer = flowTemperatureSummer;
		clone.returnTemperatureWinter = returnTemperatureWinter;
		clone.returnTemperatureSummer = returnTemperatureSummer;
		if (intervalWinter != null) {
			clone.intervalWinter = intervalWinter.copy();
		}
		if (intervalSummer != null) {
			clone.intervalSummer = intervalSummer.copy();
		}
		
		for (var p : pipes) {
			clone.pipes.add(p.copy());
		}
		return clone;
	}
}
