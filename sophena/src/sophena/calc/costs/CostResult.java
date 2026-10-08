package sophena.calc.costs;

import java.util.ArrayList;
import java.util.List;

public class CostResult {

	public final List<CostResultItem> items = new ArrayList<>();

	public final FieldSet dynamicTotal = new FieldSet();
	public final FieldSet staticTotal = new FieldSet();

	public static class FieldSet {

		/// The total initial investment sum in EUR.
		public double investments;

		/// The total funding in EUR.
		public double funding;

		/// Annuity of capital tied up in the investment.
		/// @de Kapitalgebundene Kosten
		public double capitalCosts;

		/// Annuity of fuel, energy, and material costs.
		/// @de Verbrauchsgebundene Kosten
		public double consumptionCosts;

		/// Annuity of operation, maintenance, and repair.
		/// @de Betriebsgebundene Kosten
		public double operationCosts;

		/// Insurance, administration, and other fixed annual costs. */
		/// @de Sonstige Kosten
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
