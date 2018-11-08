package sophena.io.excel;

import java.util.Arrays;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import sophena.Labels;
import sophena.calc.ProjectLoad;
import sophena.calc.ProjectResult;
import sophena.math.energetic.GeneratedHeat;
import sophena.math.energetic.Producers;
import sophena.math.energetic.UtilisationRate;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.Project;
import sophena.model.Stats;

class HeatSheet {

	private final Workbook wb;
	private final ProjectResult result;
	private final SheetWriter w;

	HeatSheet(Workbook wb, ProjectResult result) {
		this.wb = wb;
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
			w.rint(p.boiler.maxPower);
			w.str(getFuelUse(p, heat));
			w.rint(heat);
			w.rint(GeneratedHeat.share(heat, result.energyResult));
			w.rint(Producers.fullLoadHours(p, heat));
			w.rint(UtilisationRate.get(
					result.project, p, result.energyResult) * 100);
			w.num(result.energyResult.numberOfStarts(p));
			row++;
		}
		powerDiffRow(row, result.energyResult.producers);
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

	private void powerDiffRow(int row, Producer[] producers) {
		double diff = calculateDiff(producers);
		double powerDiff = calculatePowerDiff(producers, result.project);
		if (diff != 0 || powerDiff < 0) {
			w.str(row, 0, "Ungedeckte Leistung");
			w.rint(2, -powerDiff);
			w.rint(4, -diff);
			w.rint(5, GeneratedHeat.share(diff, result.energyResult));
			row++;
		}
		row++;
		w.str(row, 0, "Pufferspeicher");
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

	private double calculatePowerDiff(Producer[] producers, Project p) {
		double maxLoad;
		maxLoad = ProjectLoad.getMax(p);
		if (p.heatNet != null)
			maxLoad = Math.ceil(maxLoad * p.heatNet.simultaneityFactor);

		double power = 0;
		for (Producer producer : producers) {
			power += Producers.maxPower(producer);
		}
		return power - maxLoad;
	}

}
