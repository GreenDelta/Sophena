package sophena.io.excel;

import java.util.Arrays;

import org.apache.poi.ss.usermodel.Workbook;

import sophena.Labels;
import sophena.calc.ProjectResult;
import sophena.math.energetic.GeneratedElectricity;
import sophena.math.energetic.Producers;
import sophena.model.Producer;
import sophena.model.ProducerFunction;

class ElectricitySheet {

	private final ProjectResult result;
	private final SheetWriter w;

	ElectricitySheet(Workbook wb, ProjectResult result) {
		this.result = result;
		w = new SheetWriter(wb, "Strom");
	}

	void write() {

		w.boldStr("Wärmeerzeuger")
				.boldStr("Rang")
				.boldStr("Nennleistung [kW]")
				.boldStr("Erzeugter Strom [kWh]")
				.boldStr("Anteil [%]")
				.boldStr("Volllaststunden [h]")
				.boldStr("Wirkungsgrad [%]")
				.nextRow();

		Arrays.sort(result.energyResult.producers,
				(r1, r2) -> Integer.compare(r1.rank, r2.rank));
		double total = calculateTotal(result.energyResult.producers);

		for (Producer p : result.energyResult.producers) {
			if (!Producers.isCoGenPlant(p))
				continue;
			double heat = result.energyResult.totalHeat(p);
			double value = GeneratedElectricity.get(p, result);
			w.str(p.name);
			w.str(Labels.getRankText(p.function, p.rank));

			w.rint(Producers.electricPower(p))
					.rint(value)
					.rint((value / total) * 100)
					.rint(Producers.fullLoadHours(p, heat))
					.rint(Producers.electricalEfficiency(p) * 100d)
					.nextRow();
		}

		Excel.autoSize(w.sheet, 0, 6);
	}

	private double calculateTotal(Producer[] producers) {
		double total = 0d;
		for (Producer pr : producers) {
			total += GeneratedElectricity.get(pr, result);
		}
		return total;
	}

}
