package sophena.model;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 'Factor' in this class means 'price change factor'.
 */
@Entity
@Table(name = "tbl_cost_settings")
public class CostSettings extends AbstractEntity {

	private double investmentFactor;
	private double otherFactor;
	private double hourlyWage;
	private double bioFuelFactor;
	private double fossilFuelFactor;
	private double electricityFactor;
	private double maintenanceFactor;
	private double vatRate;
	private double insuranceShare;
	private double taxShare;
}
