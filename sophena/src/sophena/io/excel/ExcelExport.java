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

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final ProjectResult result;
	private final Project project;
	private final File file;

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
			log.info("Export results to file {}", file);
			Workbook wb = new XSSFWorkbook();
			log.trace("Write heat results");
			new HeatSheet(wb, result).write();
			log.trace("Write electricity results");
			new ElectricitySheet(wb, result).write();
			log.trace("Write cost results");
			new CostSheet(wb, result).write();
			log.trace("Write further results");
			new FurtherResultsSheet(wb, result, project).write();
			log.trace("Write consumers");
			new ConsumerSheet(wb, result, project).write();
			log.trace("Write simulation results");
			new SimulationSheet(wb, result.energyResult).write();

			try (FileOutputStream fos = new FileOutputStream(file);
					BufferedOutputStream buffer = new BufferedOutputStream(
							fos)) {
				log.trace("Write workbook to file {}", file);
				wb.write(buffer);
			}
			log.trace("Export done");
		} catch (Exception e) {
			log.error("Failed to export results to Excel", e);
		}
	}
}
