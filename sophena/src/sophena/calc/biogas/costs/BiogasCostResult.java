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

		/// The funding for the electricity feed-in. The price change factor of
		/// the electricity revenues is not applied to this value.
		/// @de Einspeiseabhängige Förderung
		public double feedInFundingRevenues;

		/// The revenues at the monthly market value. The price change factor of
		/// the electricity revenues is not applied to this value.
		/// @de Stromerlöse - Referenzmarktwert
		public double referenceMarketRevenues;

		/// The additional revenues: the difference between the exchange revenues
		/// and the revenues at the monthly market value. The price change factor of
		/// the electricity revenues is only applied to this value. It can be
		/// negative.
		/// @de Stromerlöse - Mehrerlöse
		public double additionalRevenues;

		/// The payment to the direct marketer: the share of the additional
		/// revenues, but never negative.
		/// @de Direktvermarkterzahlungen
		public double directMarketerPayment;

		/// Revenues from generated electricity: the feed-in funding plus the
		/// reference market revenues plus the additional revenues minus the
		/// payment to the direct marketer.
		public double revenuesElectricity;

		/// Revenues from generated heat. These are calculated from the suggested
		/// heat price and the heat that can be sold to the heat network.
		public double revenuesHeat;

		/// The total annual revenues: the sum of the annual funding, the
		/// electricity revenues, and the heat revenues.
		public double totalRevenues;

		/// The suggested heat price in EUR/MWh that is needed so that the annual
		/// surplus reaches the expected profit of the plant settings (the total
		/// capital return rate or the expected annual surplus).
		public double suggestedHeatPrice;

	}

}
