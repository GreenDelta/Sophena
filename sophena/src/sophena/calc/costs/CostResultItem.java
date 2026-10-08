package sophena.calc.costs;

import java.util.Objects;

import org.jspecify.annotations.NullMarked;

import sophena.model.ProductType;

@NullMarked
public record CostResultItem(
	InvestmentItem investment,
	double capitalCosts,
	double demandRelatedCosts,
	double operationRelatedCosts
) {

	public CostResultItem {
		Objects.requireNonNull(investment);
	}

	public String asset() {
		return investment.asset();
	}

	public ProductType productType() {
		return investment.productType();
	}

	public double initialInvestment() {
		return investment.initialInvestment();
	}

}
