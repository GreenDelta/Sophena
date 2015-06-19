package sophena.calc;

public class CostResult {

	public final FieldSet netto = new FieldSet();
	public final FieldSet brutto = new FieldSet();

	public class FieldSet {

		public double investments;
		public double capitalCosts;
		public double capitalCostsFunding;
		public double consumptionCosts;
		public double operationCosts;
		public double otherCosts;
		public double revenues;

		public double annualCosts;
		public double annualCostsFunding;
		public double heatGenerationCosts;
		public double heatGenerationCostsFunding;

	}

}
