package sophena.math.energetic;

public class FuelEnergy {

	private final double amount;
	private double calorificValue;

	private FuelEnergy(double amount) {
		this.amount = amount;
	}

	public static FuelEnergy ofAmount_unit(double amount) {
		return new FuelEnergy(amount);
	}

	public FuelEnergy calorificValue_kWh_per_unit(double calorificValue) {
		this.calorificValue = calorificValue;
		return this;
	}

	public double get_kWh() {
		return amount * calorificValue;
	}

}
