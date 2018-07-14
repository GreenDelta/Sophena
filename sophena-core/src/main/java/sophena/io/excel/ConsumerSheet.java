package sophena.io.excel;

import java.util.Collections;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import sophena.calc.ConsumerResult;
import sophena.calc.ProjectLoad;
import sophena.calc.ProjectResult;
import sophena.model.Project;
import sophena.utils.Strings;

class ConsumerSheet {

	private Workbook wb;
	private ProjectResult result;
	private Project project;

	ConsumerSheet(Workbook wb, ProjectResult result, Project project) {
		this.wb = wb;
		this.result = result;
		this.project = project;
	}

	void write() {
		Sheet sheet = wb.createSheet("Abnehmer");
		header(sheet);
		int row = 1;
		double totalDemand = 0;
		double totalLoad = 0;
		Collections.sort(result.consumerResults,
				(r1, r2) -> Strings.compare(r1.consumer.name, r2.consumer.name));
		for (ConsumerResult cr : result.consumerResults) {
			totalDemand += cr.heatDemand;
			totalLoad += cr.consumer.heatingLoad;
			Excel.cell(sheet, row, 0, cr.consumer.name);
			Excel.cell(sheet, row, 1, Math.round(cr.consumer.heatingLoad));
			Excel.cell(sheet, row, 2, Math.round(cr.heatDemand));
			row++;
		}
		netRow(sheet, row);
		totalRow(sheet, row + 1, totalDemand, totalLoad);
		Excel.autoSize(sheet, 0, 2);
	}

	private void netRow(Sheet sheet, int row) {
		Excel.cell(sheet, row, 0, "Netzverluste");
		Excel.cell(sheet, row, 1, Math.round(ProjectLoad.getNetLoad(project.heatNet)));
		Excel.cell(sheet, row, 2, Math.round(result.energyResult.heatNetLoss));
	}

	private void totalRow(Sheet sheet, int row, double totalDemand, double totalLoad) {
		CellStyle style = Excel.headerStyle(wb);
		Excel.cell(sheet, row, 0, "Gesamter Wärmebedarf")
				.setCellStyle(style);
		Excel.cell(sheet, row, 1, Math.round(totalLoad + ProjectLoad.getNetLoad(project.heatNet)))
				.setCellStyle(style);
		Excel.cell(sheet, row, 2, Math.round((totalDemand + result.energyResult.heatNetLoss)))
				.setCellStyle(style);
	}

	private void header(Sheet sheet) {
		CellStyle style = Excel.headerStyle(wb);
		Excel.cell(sheet, 0, 0, "Abnehmer").setCellStyle(style);
		Excel.cell(sheet, 0, 1, "Heizlast in kW").setCellStyle(style);
		Excel.cell(sheet, 0, 2, "Wärmebedarf in kWh").setCellStyle(style);
	}

}
