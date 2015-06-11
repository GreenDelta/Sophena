package sophena.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ComponentCosts {

	@Column(name = "investment")
	private double investment;

	@Column(name = "duration")
	private int duration;

	@Column(name = "repair")
	private double repair;

	@Column(name = "maintenance")
	private double maintenance;

	@Column(name = "operation")
	private double operation;

	public double getInvestment() {
		return investment;
	}

	public void setInvestment(double investment) {
		this.investment = investment;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public double getRepair() {
		return repair;
	}

	public void setRepair(double repair) {
		this.repair = repair;
	}

	public double getMaintenance() {
		return maintenance;
	}

	public void setMaintenance(double maintenance) {
		this.maintenance = maintenance;
	}

	public double getOperation() {
		return operation;
	}

	public void setOperation(double operation) {
		this.operation = operation;
	}

	@Override
	public ComponentCosts clone() {
	   ComponentCosts clone = new ComponentCosts();
		clone.duration = duration;
		clone.investment = investment;
		clone.maintenance = maintenance;
		clone.operation = operation;
		clone.repair = repair;
		return clone;
	}
}
