package sophena.calc.costs;

import sophena.model.ProductCosts;
import sophena.model.ProductEntry;
import sophena.model.biogas.BiogasInvestmentEntry;

/// A facade for the investment data that is used in the cost calculations of
/// projects and biogas plants. It adapts the different model classes
/// (`ProductEntry`, `BiogasInvestmentEntry`) to a common interface.
public sealed interface InvestmentItem {

	/// The full investment in EUR. This is the amount that is spent when the
	/// asset is replaced after its lifetime.
	double investment();

	/// The amount in EUR that is spent initially. For the refurbishment or
	/// general overhaul of an existing asset this can be a share of the full
	/// investment.
	double initialInvestment();

	/// The usage duration of the asset in years.
	int duration();

	/// The fraction [%] of the investment that is used for repair.
	double repair();

	/// The fraction [%] of the investment that is used for maintenance.
	double maintenance();

	/// The hours per year that are used for the operation of the asset.
	double operation();

	/// Returns a facade for the given biogas investment entry. A `null` entry
	/// is mapped to an empty item.
	static InvestmentItem of(BiogasInvestmentEntry entry) {
		return entry == null ? new EmptyItem() : new BiogasItem(entry);
	}

	/// Returns a facade for the given product entry. A `null` entry is mapped
	/// to an empty item.
	static InvestmentItem of(ProductEntry entry) {
		return entry == null ? new EmptyItem() : new ProductItem(entry);
	}

	/// An investment item without any investments.
	record EmptyItem() implements InvestmentItem {

		@Override
		public double investment() {
			return 0;
		}

		@Override
		public double initialInvestment() {
			return 0;
		}

		@Override
		public int duration() {
			return 0;
		}

		@Override
		public double repair() {
			return 0;
		}

		@Override
		public double maintenance() {
			return 0;
		}

		@Override
		public double operation() {
			return 0;
		}
	}

	/// A facade for the investment entries of a biogas plant.
	record BiogasItem(BiogasInvestmentEntry entry) implements InvestmentItem {

		@Override
		public double investment() {
			var costs = costs();
			return costs == null ? 0 : costs.investment;
		}

		@Override
		public double initialInvestment() {
			double investment = investment();
			var share = entry.refurbishmentShare;
			return share == null
				? investment
				: investment * share / 100;
		}

		@Override
		public int duration() {
			var costs = costs();
			return costs == null ? 0 : costs.duration;
		}

		@Override
		public double repair() {
			var costs = costs();
			return costs == null ? 0 : costs.repair;
		}

		@Override
		public double maintenance() {
			var costs = costs();
			return costs == null ? 0 : costs.maintenance;
		}

		@Override
		public double operation() {
			var costs = costs();
			return costs == null ? 0 : costs.operation;
		}

		private ProductCosts costs() {
			return entry == null ? null : entry.costs;
		}
	}

	/// A facade for the product entries of a project. Product entries are always
	/// invested completely.
	record ProductItem(ProductEntry entry) implements InvestmentItem {

		@Override
		public double investment() {
			var costs = costs();
			return costs == null ? 0 : costs.investment;
		}

		@Override
		public double initialInvestment() {
			return investment();
		}

		@Override
		public int duration() {
			var costs = costs();
			return costs == null ? 0 : costs.duration;
		}

		@Override
		public double repair() {
			var costs = costs();
			return costs == null ? 0 : costs.repair;
		}

		@Override
		public double maintenance() {
			var costs = costs();
			return costs == null ? 0 : costs.maintenance;
		}

		@Override
		public double operation() {
			var costs = costs();
			return costs == null ? 0 : costs.operation;
		}

		private ProductCosts costs() {
			return entry == null ? null : entry.costs;
		}
	}
}
