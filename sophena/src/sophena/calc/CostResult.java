package sophena.calc;

public class CostResult {

	public final FieldSet netTotal = new FieldSet();
	public final FieldSet grossTotal = new FieldSet();

	public class FieldSet {

		public double investments;
		public double capitalCosts;
		public double consumptionCosts;
		public double operationCosts;
		public double otherCosts;
		public double revenues;
		public double annualCosts;
		public double heatGenerationCosts;

	}

}
