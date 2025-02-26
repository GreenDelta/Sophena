package sophena.io.excel;

import java.util.Collections;

import org.apache.poi.ss.usermodel.Workbook;

import sophena.calc.ConsumerResult;
import sophena.calc.ProjectLoad;
import sophena.calc.ProjectResult;
import sophena.model.Project;
import sophena.utils.Strings;

class ConsumerSheet {

	private final ProjectResult result;
	private final SheetWriter w;

	ConsumerSheet(Workbook wb, ProjectResult result, Project project) {
		this.result = result;
		w = new SheetWriter(wb, "Abnehmer");
	}

	void write() {

		w.boldStr("Abnehmer")
				.boldStr("Heizlast [kW]")
				.boldStr("Wärmebedarf [kWh]")
				.nextRow();

		// init totals
		double load = 0;
		double demand = 0;

		// consumer results
		Collections.sort(result.consumerResults,
				(r1, r2) -> Strings.compare(
						r1.consumer.name,
						r2.consumer.name));
		for (ConsumerResult cr : result.consumerResults) {
			demand += cr.heatDemand;
			load += cr.consumer.heatingLoad;
			w.str(cr.consumer.name)
					.rint(cr.consumer.heatingLoad)
					.rint(cr.heatDemand)
					.nextRow();
		}

		// net result
		double netLoad = ProjectLoad.getMaxNetLoad(
				result.project);
		load += netLoad;
		demand += result.energyResult.heatNetLoss;
		w.str("Netzverluste")
				.rint(netLoad)
				.rint(result.energyResult.heatNetLoss)
				.nextRow();

		// buffer loss
		demand += result.energyResult.totalBufferLoss;
		w.str("Pufferspeicherverluste")
				.nextCol()
				.rint(result.energyResult.totalBufferLoss)
				.nextRow();

		// totals
		w.boldStr("Summe")
				.boldRint(load)
				.boldRint(demand);

		Excel.autoSize(w.sheet, 0, 2);
	}

}
