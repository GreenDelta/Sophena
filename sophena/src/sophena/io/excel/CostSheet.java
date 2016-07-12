package sophena.io.excel;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import sophena.calc.CostResult;
import sophena.calc.CostResultItem;
import sophena.calc.ProjectResult;
import sophena.rcp.Labels;
import sophena.rcp.utils.Strings;

class CostSheet {

	private Workbook wb;
	private ProjectResult result;
	public int row = 0;

	CostSheet(Workbook wb, ProjectResult result) {
		this.wb = wb;
		this.result = result;
	}

	void write() {
		Sheet sheet = wb.createSheet("Wirtschaftlichkeit");
		CellStyle style = Excel.headerStyle(wb);

		resultFunding(sheet, style, row);
		costResultFunding(sheet, style, row);
		result(sheet, style, row);
		costResult(sheet, style, row);

		Excel.autoSize(sheet, 0, 1);
	}

	private void resultFunding(Sheet sheet, CellStyle style, int row) {
		Excel.cell(sheet, row + 0, 0,
				"Wirtschaftlichkeit - mit Förderung").setCellStyle(style);
		resultHead(sheet, style, row);
		resultItems(sheet, row, result.costResultFunding);
		this.row = 11;
	}

	private void costResultFunding(Sheet sheet, CellStyle style, int row) {

		Excel.cell(sheet, row, 0, "Kosten - mit Förderung").setCellStyle(style);
		row++;
		costResultHead(sheet, style, row);
		row++;
		createItems(sheet, style, row, true);
	}

	private void result(Sheet sheet, CellStyle style, int row) {

		Excel.cell(sheet, row, 0,
				"Wirtschaftlichkeit - ohne Förderung").setCellStyle(style);
		resultHead(sheet, style, row);
		resultItems(sheet, row, result.costResult);
		this.row = row + 11;
	}

	private void costResult(Sheet sheet, CellStyle style, int row) {

		Excel.cell(sheet, row, 0,
				"Kosten - ohne Förderung").setCellStyle(style);
		row++;
		costResultHead(sheet, style, row);
		row++;
		createItems(sheet, style, row, false);
	}

	private void resultHead(Sheet sheet, CellStyle style, int row) {

		Excel.cell(sheet, row + 1, 1, "Netto").setCellStyle(style);
		Excel.cell(sheet, row + 1, 2, "Brutto").setCellStyle(style);
		Excel.cell(sheet, row + 2, 0, "Investitionskosten in EUR");
		Excel.cell(sheet, row + 3, 0, "Kapitalgebundene Kosten in EUR/a");
		Excel.cell(sheet, row + 4, 0, "Bedarfsgebundene Kosten in EUR/a");
		Excel.cell(sheet, row + 5, 0, "Betriebsgebundene Kosten in EUR/a");
		Excel.cell(sheet, row + 6, 0, "Sonstige Kosten in EUR/a");
		Excel.cell(sheet, row + 7, 0, "Stromerlöse in EUR/a");
		Excel.cell(sheet, row + 8, 0, "Kosten - Erlöse in EUR/a");
		Excel.cell(sheet, row + 9, 0, "Wärmegestehungskosten in EUR/MWh");
	}

	private void costResultHead(Sheet sheet, CellStyle style, int row) {

		Excel.cell(sheet, row, 0, "Produktbereich");
		Excel.cell(sheet, row, 1, "Produkt");
		Excel.cell(sheet, row, 2, "Investitionskosten in EUR");
		Excel.cell(sheet, row, 3, "Kapitalgebundene Kosten in EUR/a");
		Excel.cell(sheet, row, 4, "Bedarfsgebundene Kosten in EUR/a");
		Excel.cell(sheet, row, 5, "Betriebsgebundene Kosten in EUR/a");
	}

	private void resultItems(Sheet sheet, int row, CostResult cr) {

		Excel.cell(sheet, row + 2, 1, Math.round(cr.netTotal.investments));
		Excel.cell(sheet, row + 2, 2, Math.round(cr.grossTotal.investments));
		Excel.cell(sheet, row + 3, 1, Math.round(cr.netTotal.capitalCosts));
		Excel.cell(sheet, row + 3, 2, Math.round(cr.grossTotal.capitalCosts));
		Excel.cell(sheet, row + 4, 1, Math.round(cr.netTotal.consumptionCosts));
		Excel.cell(sheet, row + 4, 2, Math.round(cr.grossTotal.consumptionCosts));
		Excel.cell(sheet, row + 5, 1, Math.round(cr.netTotal.operationCosts));
		Excel.cell(sheet, row + 5, 2, Math.round(cr.grossTotal.operationCosts));
		Excel.cell(sheet, row + 6, 1, Math.round(cr.netTotal.otherCosts));
		Excel.cell(sheet, row + 6, 2, Math.round(cr.grossTotal.otherCosts));
		Excel.cell(sheet, row + 7, 1, Math.round(cr.netTotal.revenues));
		Excel.cell(sheet, row + 7, 2, Math.round(cr.grossTotal.revenues));
		Excel.cell(sheet, row + 8, 1, Math.round(cr.netTotal.annualCosts));
		Excel.cell(sheet, row + 8, 2, Math.round(cr.grossTotal.annualCosts));
		Excel.cell(sheet, row + 9, 1, Math.round((cr.netTotal.heatGenerationCosts
				* 1000)));
		Excel.cell(sheet, row + 9, 2, Math.round((cr.grossTotal.heatGenerationCosts
				* 1000)));
	}

	private class Item {
		String category;
		String product;
		double investment;
		double capitalCosts;
		double consumptionCosts;
		double operationCosts;
	}

	private void createItems(Sheet sheet, CellStyle style, int row, boolean isFunded) {
		List<Item> items = new ArrayList<>();
		List<CostResultItem> cri;
		if (isFunded) {
			cri = result.costResultFunding.items;
		} else {
			cri = result.costResult.items;
		}
		for (CostResultItem r : cri) {
			Item item = new Item();
			item.category = Labels.get(r.productType);
			item.product = r.label;
			item.investment = r.costs.investment;
			item.capitalCosts = r.netCapitalCosts;
			item.consumptionCosts = r.netConsumtionCosts;
			item.operationCosts = r.netOperationCosts;
			items.add(item);
		}
		sortAndRefine(items);

		for (int i = 0; i < items.size(); i++) {
			Excel.cell(sheet, row, 0, items.get(i).category);
			Excel.cell(sheet, row, 1, items.get(i).product);
			Excel.cell(sheet, row, 2, Math.round(items.get(i).investment));
			Excel.cell(sheet, row, 3, Math.round(items.get(i).capitalCosts));
			Excel.cell(sheet, row, 4, Math.round(items.get(i).consumptionCosts));
			Excel.cell(sheet, row, 5, Math.round(items.get(i).operationCosts));
			row++;
		}
		row++;
		this.row = row;
	}

	private void sortAndRefine(List<Item> items) {
		items.sort((a, b) -> {
			int c = Strings.compare(a.category, b.category);
			if (c != 0)
				return c;
			return Strings.compare(a.product, b.product);
		});
	}

}
