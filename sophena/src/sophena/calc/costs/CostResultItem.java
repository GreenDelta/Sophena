package sophena.calc.costs;

import java.util.Objects;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class CostResultItem {

	public final InvestmentItem investment;
	public double capitalCosts;
	public double demandRelatedCosts;
	public double operationRelatedCosts;

	CostResultItem(InvestmentItem investment) {
		this.investment = Objects.requireNonNull(investment);
	}
}
