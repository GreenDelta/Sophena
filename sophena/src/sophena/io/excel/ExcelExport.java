package sophena.io.excel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.calc.ProjectResult;
import sophena.model.Project;

public class ExcelExport implements Runnable {

	private ProjectResult result;
	private Project project;
	private File file;

	public ExcelExport(Project project, ProjectResult result, File file) {
		this.result = result;
		this.file = file;
		this.project = project;
	}

	@Override
	public void run() {
		if (result == null || file == null)
			return;
		try {
			Workbook wb = new XSSFWorkbook();
			new HeatSheet(wb, result, project).write();
			new ElectricitySheet(wb, result).write();
			new CostSheet(wb, result).write();
			new FurtherResultsSheet(wb, result, project).write();
			new ConsumerSheet(wb, result, project).write();
			new SimulationSheet(wb, result.energyResult).write();

			try (FileOutputStream fos = new FileOutputStream(file);
					BufferedOutputStream buffer = new BufferedOutputStream(fos)) {
				wb.write(buffer);
			}
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Failed to export results to Excel", e);
		}
	}
}
