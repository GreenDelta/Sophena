package sophena.io.excel;

import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import sophena.calc.ProjectResult;
import sophena.math.energetic.CO2Emissions;
import sophena.math.energetic.EfficiencyResult;
import sophena.math.energetic.PrimaryEnergyFactor;
import sophena.math.energetic.UsedHeat;
import sophena.model.Producer;
import sophena.model.Project;

class FurtherResultsSheet {

	private Workbook wb;
	private ProjectResult result;
	private Project project;

	public int row = 0;

	FurtherResultsSheet(Workbook wb, ProjectResult result) {
		this.wb = wb;
		this.result = result;

	}

	void write() {

		CO2Emissions co2 = CO2Emissions.calculate(project, result);
		EfficiencyResult efficiency = EfficiencyResult.calculate(result);
		Sheet sheet = wb.createSheet("Weitere Ergebnisse");
		CellStyle style = Excel.headerStyle(wb);

		createEmissions(sheet, style, row, co2);
		createEfficiency(sheet, style, row, efficiency);
		heatNetInfo(sheet, style, row);

		Excel.autoSize(sheet, 0, 1);
	}

	public void createEmissions(Sheet sheet, CellStyle style, int row, CO2Emissions co2) {

		Excel.cell(sheet, row, 0, "Treibhausgasemissionen").setCellStyle(style);
		row++;
		Excel.cell(sheet, row, 1, "Emissionen in kg CO2 eq.").setCellStyle(style);
		row++;
		Map<Producer, Double> m = co2.producerEmissions;
		for (Producer p : m.keySet()) {
			Double val = m.get(p);
			Excel.cell(sheet, row, 0, p.name);
			Excel.cell(sheet, row, 1, val.intValue());
			row++;
		}
		Excel.cell(sheet, row, 0, "Eigenstromverbrauch");
		Excel.cell(sheet, row, 1, (int) co2.electricityEmissions);
		row++;
		Excel.cell(sheet, row, 0, "Gutschrift Stromerzeugung");
		double credits = co2.electricityCredits;
		if (credits > 0) {
			Excel.cell(sheet, row, 1, (int) -credits);
		}
		row += 2;
		Excel.cell(sheet, row, 0, "Wärmenetz").setCellStyle(style);
		Excel.cell(sheet, row, 1, (int) co2.total).setCellStyle(style);
		row += 2;
		Excel.cell(sheet, row, 0, "Erdgas dezentral");
		Excel.cell(sheet, row, 1, (int) co2.variantNaturalGas);
		row++;
		Excel.cell(sheet, row, 0, "Heizöl dezentral");
		Excel.cell(sheet, row, 1, (int) co2.variantOil);
		row += 2;

		this.row = row;
	}

	public void createEfficiency(Sheet sheet, CellStyle style, int row,
			EfficiencyResult efficiency) {

		Excel.cell(sheet, row, 0, "Effizienz").setCellStyle(style);
		row++;
		Excel.cell(sheet, row, 1, "Absolut in kWh").setCellStyle(style);
		Excel.cell(sheet, row, 2, "Prozentual in %").setCellStyle(style);
		row++;
		Excel.cell(sheet, row, 0, "Brennstoffenergie");
		Excel.cell(sheet, row, 1, (int) efficiency.fuelEnergy);
		row++;
		Excel.cell(sheet, row, 0, "Konversionsverluste");
		Excel.cell(sheet, row, 1, (int) efficiency.conversionLoss);
		Excel.cell(sheet, row, 2, (int) ((efficiency.conversionLoss
				/ efficiency.fuelEnergy) * 100));
		row++;
		Excel.cell(sheet, row, 0, "Erzeugte Wärme");
		Excel.cell(sheet, row, 1, (int) efficiency.producedHeat);
		if (efficiency.producedElectrictiy > 0) {
			row++;
			Excel.cell(sheet, row, 0, "Erzeugter Strom");
			Excel.cell(sheet, row, 1, (int) efficiency.producedElectrictiy);
		}
		row++;
		Excel.cell(sheet, row, 0, "Verteilungsverluste");
		Excel.cell(sheet, row, 1, (int) efficiency.distributionLoss);
		Excel.cell(sheet, row, 2, (int) ((efficiency.distributionLoss
				/ efficiency.producedHeat) * 100));
		row++;
		Excel.cell(sheet, row, 0, "Genutzte Wärme");
		Excel.cell(sheet, row, 1, (int) efficiency.usedHeat);
		row += 2;
		Excel.cell(sheet, row, 0, "Gesamtverluste").setCellStyle(style);
		Excel.cell(sheet, row, 1, (int) efficiency.totalLoss).setCellStyle(style);

		Excel.cell(sheet, row, 2, (int) ((efficiency.totalLoss
				/ efficiency.fuelEnergy) * 100)).setCellStyle(style);
		row += 2;

		this.row = row;
	}

	public void heatNetInfo(Sheet sheet, CellStyle style, int row) {
		double length;

		try {
			length = project.heatNet.length;
		} catch (Exception e) {
			length = 0;
		}

		Excel.cell(sheet, row, 0, "Kennzahlen Wärmenetz").setCellStyle(style);
		row++;
		Excel.cell(sheet, row, 0, "Trassenlänge in m");
		Excel.cell(sheet, row, 1, length);
		row++;
		Excel.cell(sheet, row, 0, "Wärmebelegungsdichte in MWh/(m*a)");
		Excel.cell(sheet, row, 1, UsedHeat.get(result) / (1000 * length));
		row++;
		Excel.cell(sheet, row, 0, "Primärenergiefaktor");
		Excel.cell(sheet, row, 1, PrimaryEnergyFactor.get(project, result));

		this.row = row;
	}

}
