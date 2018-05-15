package sophena.calc;

import java.util.ArrayList;
import java.util.List;

public class CostResult {

	public final List<CostResultItem> items = new ArrayList<>();

	public final FieldSet netTotal = new FieldSet();
	public final FieldSet grossTotal = new FieldSet();

	public static class FieldSet {

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
