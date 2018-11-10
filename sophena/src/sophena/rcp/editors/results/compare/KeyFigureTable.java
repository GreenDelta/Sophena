package sophena.rcp.editors.results.compare;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.calc.CO2Result;
import sophena.calc.Comparison;
import sophena.math.energetic.EfficiencyResult;
import sophena.math.energetic.PrimaryEnergyFactor;
import sophena.math.energetic.Producers;
import sophena.math.energetic.UsedHeat;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class KeyFigureTable {

	private final Comparison result;

	private KeyFigureTable(Comparison result) {
		this.result = result;
	}

	static KeyFigureTable of(Comparison result) {
		return new KeyFigureTable(result);
	}

	void render(Composite body, FormToolkit tk) {
		Composite comp = UI.formSection(body, tk,
				"Energetische Kennzahlen");
		Table table = new Table(result);
		createItems(table);
		table.render(comp);
	}

	private void createItems(Table table) {
		table.row("Erzeugte Wärmemenge", i -> Num.intStr(
				result.results[i].energyResult.totalProducedHeat) + " kWh");
		table.row("Installierte Leistung", this::installedPower);
		table.row("Trassenlänge",
				i -> Num.intStr(result.projects[i].heatNet.length) + " m");
		table.row("Wärmebelegungsdichte", this::heatOccupancyDensity);
		table.row("Netzverluste", this::distributionLoss);
		table.row("CO2-Einsparung (gegen Erdgas dezentral)",
				this::emissionSavings);
		table.row("Primärenergiefaktor",
				i -> Num.str(PrimaryEnergyFactor.get(result.results[i]), 2));
	}

	private String installedPower(int i) {
		Project project = result.projects[i];
		double power = 0.0;
		for (Producer p : project.producers) {
			if (p.disabled)
				continue;
			power += Producers.maxPower(p);
		}
		return Num.intStr(power) + " kW";
	}

	private String heatOccupancyDensity(int i) {
		double length = result.projects[i].heatNet.length;
		double hl = length == 0 ? 0
				: UsedHeat.get(result.results[i]) / (1000 * length);
		return Num.str(hl, 2) + " MWh/(m*a)";
	}

	private String distributionLoss(int i) {
		EfficiencyResult er = EfficiencyResult.calculate(result.results[i]);
		double loss = 0;
		if (er.fuelEnergy > 0) {
			loss = 100 * er.distributionLoss / er.fuelEnergy;
		}
		return Num.intStr(loss) + " %";
	}

	private String emissionSavings(int i) {
		CO2Result co2 = result.results[i].co2Result;
		double savings = co2.variantNaturalGas - co2.total;
		return Num.intStr(savings) + " kg CO2 eq./a";
	}

}
