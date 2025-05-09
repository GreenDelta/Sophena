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
import sophena.model.Stats;
import sophena.utils.Num;

class HeatSheet {

	private final ProjectResult result;
	private final SheetWriter w;
	
	private boolean showStagnationDays = false;		
	private boolean showJAZ = false;

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
			w.str(Labels.getRankText(p.function, p.rank));
			double maxPower = p.solarCollector != null & p.solarCollectorSpec != null ? result.energyResult.maxPeakPower(p) : Producers.maxPower(p);
			w.rint(maxPower);
			w.str(getFuelUse(p, heat));
			w.rint(heat);
			w.rint(GeneratedHeat.share(heat, result.energyResult));
			w.rint(maxPower == 0 ? 0 : (int) Math.ceil(heat / maxPower));
			if(p.heatPump != null)
				w.nextCol();
			else if (p.boiler != null && p.boiler.isCoGenPlant) {
				w.rint(100 * p.boiler.efficiencyRate);
			} else {
				w.rint(100 * UtilisationRate.get(
						result.project, p, result.energyResult));
			}
			w.num(result.energyResult.numberOfStarts(p));
			if(p.solarCollector != null & p.solarCollectorSpec != null) {				
				w.rint(result.energyResult.stagnationDays(p));
			}
			else if (showStagnationDays)
				w.nextCol();
			if(p.heatPump != null)
			{
				w.num((double)Math.round(result.energyResult.jaz(p) * 100) / 100);
			}
			else if (showJAZ)
				w.nextCol();
			row++;
		}
		diffAndBuffer(row, result.energyResult.producers);
		Excel.autoSize(w.sheet, 0, 7);
	}
	
	private void header() {
		w.boldStr(0, 0, "Wärmeerzeuger");
		w.boldStr("Rang");
		w.boldStr("Nennleistung [kW]");
		w.boldStr("Energieträgereinsatz");
		w.boldStr("Erzeugte Wärme [kWh]");
		w.boldStr("Anteil [%]");
		w.boldStr("Volllaststunden [h]");
		w.boldStr("Nutzungsgrad [%]");
		w.boldStr("Starts");
		
		for(Producer p : result.energyResult.producers)
		{
			if(p.solarCollector != null & p.solarCollectorSpec != null)
				showStagnationDays = true;
			if(p.heatPump != null)
				showJAZ = true;
		}
		if(showStagnationDays)
			w.boldStr("Stagnationstage");
		if(showJAZ)
			w.boldStr("JAZ");
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
