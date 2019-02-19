package sophena.io.excel;

import java.util.Arrays;

import org.apache.poi.ss.usermodel.Workbook;

import sophena.Labels;
import sophena.calc.ProjectLoad;
import sophena.calc.ProjectResult;
import sophena.math.energetic.GeneratedHeat;
import sophena.math.energetic.Producers;
import sophena.math.energetic.UtilisationRate;
import sophena.model.BufferTank;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.Stats;
import sophena.utils.Num;

class HeatSheet {

	private final ProjectResult result;
	private final SheetWriter w;

	HeatSheet(Workbook wb, ProjectResult result) {
		this.result = result;
		w = new SheetWriter(wb, "Wärme");
	}

	void write() {
		header();
		int row = 1;
		Arrays.sort(result.energyResult.producers,
				(r1, r2) -> Integer.compare(r1.rank, r2.rank));
		for (Producer p : result.energyResult.producers) {
			double heat = result.energyResult.totalHeat(p);
			w.str(row, 0, p.name);
			w.str(p.function == ProducerFunction.BASE_LOAD
					? p.rank + " - Grundlast"
					: p.rank + " - Spitzenlast");
			w.rint(Producers.maxPower(p));
			w.str(getFuelUse(p, heat));
			w.rint(heat);
			w.rint(GeneratedHeat.share(heat, result.energyResult));
			w.rint(Producers.fullLoadHours(p, heat));
			w.rint(UtilisationRate.get(
					result.project, p, result.energyResult) * 100);
			w.num(result.energyResult.numberOfStarts(p));
			row++;
		}
		diffAndBuffer(row, result.energyResult.producers);
		Excel.autoSize(w.sheet, 0, 7);
	}

	private void header() {
		w.boldStr(0, 0, "Wärmeerzeuger");
		w.boldStr("Rang");
		w.boldStr("Nennleistung [kW]");
		w.boldStr("Brennstoffverbrauch");
		w.boldStr("Erzeugte Wärme [kWh]");
		w.boldStr("Anteil [%]");
		w.boldStr("Volllaststunden [h]");
		w.boldStr("Nutzungsgrad [%]");
		w.boldStr("Starts");
	}

	private String getFuelUse(Producer pr, double heat) {
		return Labels.getFuel(pr) + ": "
				+ (int) result.fuelUsage.getInFuelUnits(pr)
				+ " " + Labels.getFuelUnit(pr);
	}

	private void diffAndBuffer(int row, Producer[] producers) {
		double diff = calculateDiff(producers);
		double powerDiff = Producers.powerDifference(
				producers, ProjectLoad.getSimultaneousMax(result.project));
		if (diff >= 0.5 || powerDiff < 0) {
			w.str(row, 0, "Ungedeckte Leistung");
			w.rint(2, -powerDiff);
			w.rint(4, -diff);
			w.rint(5, GeneratedHeat.share(diff, result.energyResult));
			row++;
		}

		if (result.project.heatNet == null)
			return;
		BufferTank buffer = result.project.heatNet.bufferTank;
		if (buffer == null)
			return;
		row++;
		w.str(row, 0, "Pufferspeicher");
		w.str(2, Num.intStr(buffer.volume) + " L");
		w.rint(4, result.energyResult.totalBufferedHeat);
		w.rint(5, GeneratedHeat.share(
				result.energyResult.totalBufferedHeat, result.energyResult));
	}

	private double calculateDiff(Producer[] producers) {
		double diff = 0;
		for (int i = 0; i < Stats.HOURS; i++) {
			double supplied = result.energyResult.suppliedPower[i];
			double load = result.energyResult.loadCurve[i];
			if (supplied < load)
				diff += (load - supplied);
		}
		return diff;
	}

}
