package sophena.rcp;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public enum Images {

	ADD_16("add_16.png"),

	BAR_CHART_16("bar_chart_16.png"),

	BOILER_16("boiler_16.png"),

	BUFFER_16("buffer_16.png"),

	BUILDING_TYPE_16("building_type_16.png"),

	CALCULATE_16("calculate_16.png"),

	CHART_WIZ("chart_wiz.png"),

	CHECKBOX_CHECKED_16("checkbox_checked.png"),

	CHECKBOX_UNCHECKED_16("checkbox_uncheck_16.png"),

	CLIMATE_16("climate_16.png"),

	CO_GEN_16("co_gen_16.png"),

	CONSUMER_16("consumer_16.png"),

	COPY_16("copy_16.png"),

	COSTS_16("costs_16.png"),

	DELETE_16("delete_16.png"),

	DISABLED_16("disabled_16.png"),

	EDIT_16("edit_16.png"),

	ENABLED_16("enabled_16.png"),

	ERROR_16("error_16.png"),

	EXCEL_16("excel_16.png"),

	FILE_16("file_16.png"),

	FUEL_16("fuel_16.png"),

	IMPORT_16("import_16.png"),

	INFO_16("info_16.png"),

	LOAD_PROFILE_16("load_profile_16.png"),

	NEW_16("new_16.png"),

	NEW_PROJECT_16("new_project_16.png"),

	OPEN_16("open_16.png"),

	PIPE_16("pipe_16.png"),

	PRODUCT_16("product_16.png"),

	PROJECT_16("project_16.png"),

	PRODUCER_16("producer_16.png"),

	PUMP_16("pump_16.png"),

	REQUIRED_LOAD_16("required_load_16.png"),

	RUN_16("run_16.png"),

	SORTING_16("sorting_16.png"),

	WARNING_16("warning_16.png");

	private final String fileName;

	private Images(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public ImageDescriptor des() {
		return ImageManager.getImageDescriptor(this);
	}

	public Image img() {
		return ImageManager.getImage(this);
	}

}
