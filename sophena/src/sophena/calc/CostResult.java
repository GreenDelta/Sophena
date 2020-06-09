package sophena.calc;

import java.util.ArrayList;
import java.util.List;

public class CostResult {

	public final List<CostResultItem> items = new ArrayList<>();

	public final FieldSet dynamicTotal = new FieldSet();
	public final FieldSet staticTotal = new FieldSet();

	public static class FieldSet {

		public double investments;
		public double funding;

		public double capitalCosts;
		public double consumptionCosts;
		public double operationCosts;
		public double otherAnnualCosts;

		/**
		 * The total annual costs which is just the sum of the capital costs,
		 * consumption costs, operation costs, and other annual costs.
		 */
		public double totalAnnualCosts;

		/** Revenues from generated electricity. */
		public double revenuesElectricity;

		/** Revenues from generated heat. */
		public double revenuesHeat;

		/** The annual surplus in EUR: = revenues - costs */
		public double annualSurplus;

		/** The heat generation costs in EUR/MWh */
		public double heatGenerationCosts;

	}

}
