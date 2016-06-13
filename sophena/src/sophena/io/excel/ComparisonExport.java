package sophena.io.excel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.calc.Comparison;

public class ComparisonExport implements Runnable {

	private Comparison comparison;
	private File file;

	public ComparisonExport(Comparison comparison, File file) {
		this.comparison = comparison;
		this.file = file;
	}

	@Override
	public void run() {
		if (comparison == null || file == null)
			return;
		try {
			Workbook wb = new XSSFWorkbook();
			new ComparisonSheet(wb, comparison).write();

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
