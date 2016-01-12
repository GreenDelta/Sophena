package sophena.io.excel;

import java.util.Collections;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import sophena.calc.ConsumerResult;
import sophena.calc.ProjectResult;
import sophena.rcp.utils.Strings;

class ConsumerSheet {

	private Workbook wb;
	private ProjectResult result;

	ConsumerSheet(Workbook wb, ProjectResult result) {
		this.wb = wb;
		this.result = result;
	}

	void write() {
		Sheet sheet = wb.createSheet("Abnehmer");
		header(sheet);
		int row = 1;
		double consumerTotal = 0;
		Collections.sort(result.consumerResults,
				(r1, r2) -> Strings.compare(r1.consumer.name, r2.consumer.name));
		for (ConsumerResult cr : result.consumerResults) {
			consumerTotal += cr.heatDemand;
			Excel.cell(sheet, row, 0, cr.consumer.name);
			Excel.cell(sheet, row, 1, (int) cr.heatDemand);
			row++;
		}
		netRow(sheet, row);
		totalRow(sheet, row + 1, consumerTotal);
		Excel.autoSize(sheet, 0, 1);
	}

	private void netRow(Sheet sheet, int row) {
		Excel.cell(sheet, row, 0, "Netzverluste");
		Excel.cell(sheet, row, 1, (int) result.heatNetLoss);
	}

	private void totalRow(Sheet sheet, int row, double consumerTotal) {
		CellStyle style = Excel.headerStyle(wb);
		Excel.cell(sheet, row, 0, "Gesamter Wärmebedarf")
				.setCellStyle(style);
		Excel.cell(sheet, row, 1, (int) (result.heatNetLoss + consumerTotal))
				.setCellStyle(style);
	}

	private void header(Sheet sheet) {
		CellStyle style = Excel.headerStyle(wb);
		Excel.cell(sheet, 0, 0, "Abnehmer").setCellStyle(style);
		Excel.cell(sheet, 0, 1, "Wärmebedarf").setCellStyle(style);
	}

}
