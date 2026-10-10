package sophena.calc.biogas.costs;

import java.util.ArrayList;
import java.util.List;

import sophena.calc.costs.CostResultItem;

/// The cost result of a biogas plant. It has the same structure as the cost
/// result of a project (a dynamic and a static field set) but contains the KPIs
/// that are relevant for biogas plants, e.g. no heat generation costs.
public class BiogasCostResult {

	public final List<CostResultItem> items = new ArrayList<>();

	public final Summary dynamicTotal = new Summary();
	public final Summary staticTotal = new Summary();

	public static class Summary {

		/// The total initial investment sum in EUR.
		public double investments;

		/// The total investment funding in EUR.
		public double investmentFunding;

		/// Annuity of capital tied up in the investment.
		/// @de Kapitalgebundene Kosten
		public double capitalCosts;

		/// Annuity of fuel, energy, and material costs.
		/// @de Verbrauchsgebundene Kosten
		public double consumptionCosts;

		/// Annuity of operation, maintenance, and repair.
		/// @de Betriebsgebundene Kosten
		public double operationCosts;

		/// Insurance, administration, and other fixed annual costs.
		/// @de Sonstige Kosten
		public double otherAnnualCosts;

		/// The total annual costs which is just the sum of the capital costs,
		/// consumption costs, operation costs, and other annual costs.
		public double totalAnnualCosts;

		public double annualFunding;

		/// Revenues from generated electricity.
		public double revenuesElectricity;

		/// Revenues from generated heat.
		public double revenuesHeat;

		/// The annual surplus in EUR: = revenues - costs
		public double annualSurplus;

	}

}
