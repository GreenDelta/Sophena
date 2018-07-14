package sophena.calc;

import java.util.ArrayList;
import java.util.List;

public class CostResult {

	public final List<CostResultItem> items = new ArrayList<>();

	public final FieldSet netTotal = new FieldSet();
	public final FieldSet grossTotal = new FieldSet();

	public static class FieldSet {

		public double investments;
		public double funding;
		public double capitalCosts;
		public double consumptionCosts;
		public double operationCosts;
		public double otherCosts;

		/** Revenues from generated electricity. */
		public double revenuesElectricity;

		/** Revenues from generated heat. */
		public double revenuesHeat;

		public double annualCosts;

		/** The heat generation costs in EUR/MWh */
		public double heatGenerationCosts;

	}

}
