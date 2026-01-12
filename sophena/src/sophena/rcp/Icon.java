package sophena.rcp;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/// Common icon color is: #4472a2
public enum Icon {

	ADD_16("add_16.png"),

	BAR_CHART_16("bar_chart_16.png"),

	BIOGAS_SUBSTRATE_16("biogas_substrate_16.png"),

	BOILER_16("boiler_16.png"),

	BUFFER_16("buffer_16.png"),

	BUILDING_TYPE_16("building_type_16.png"),

	CALCULATE_16("calculate_16.png"),

	CAMERA_16("camera_16.png"),

	CHART_WIZ("chart_wiz.png"),

	CHECKBOX_CHECKED_16("checkbox_checked.png"),

	CHECKBOX_UNCHECKED_16("checkbox_uncheck_16.png"),

	CLIMATE_16("climate_16.png"),

	CO_GEN_16("co_gen_16.png"),

	CONSUMER_16("consumer_16.png"),

	COPY_16("copy_16.png"),

	COSTS_16("costs_16.png"),

	DATA_TABLE_16("data_table_16.png"),

	DELETE_16("delete_16.png"),

	DELETE_DISABLED_16("delete_disabled_16.png"),

	DISABLED_16("disabled_16.png"),

	EDIT_16("edit_16.png"),

	ELECTRICITY_16("electricity_16.png"),

	ENABLED_16("enabled_16.png"),

	ERROR_16("error_16.png"),

	EXCEL_16("excel_16.png"),

	EXPORT_16("export_16.png"),

	EXPORT_FILE_16("export_file_16.png"),

	FILE_16("file_16.png"),

	FOLDER_16("folder_16.png"),

	FLUE_GAS_16("flue_gas_16.png"),

	FUEL_16("fuel_16.png"),

	HEAT_PUMP_16("heat_pump_16.png"),

	HEAT_RECOVERY_16("heat_recovery_16.png"),

	IMPORT_16("import_16.png"),

	INFO_16("info_16.png"),

	LOAD_PROFILE_16("load_profile_16.png"),

	LOCK_16("lock_16.png"),

	MANUFACTURER_16("manufacturer_16.png"),

	NAVIGATION_16("navigation_16.png"),

	NEW_16("new_16.png"),

	NEW_PROJECT_16("new_project_16.png"),

	OPEN_16("open_16.png"),

	PASTE_16("paste_16.png"),

	PIPE_16("pipe_16.png"),

	PRODUCT_16("product_16.png"),

	PROJECT_16("project_16.png"),

	PRODUCER_16("producer_16.png"),

	PUMP_16("pump_16.png"),

	REFRESH_16("refresh_16.png"),

	RENAME_16("rename_16.png"),

	REQUIRED_LOAD_16("required_load_16.png"),

	RUN_16("run_16.png"),

	SEARCH_16("search_16.png"),

	SETTINGS_16("settings_16.png"),

	SORTING_16("sorting_16.png"),

	WARNING_16("warning_16.png"),

	WEBLINK_16("weblink_16.png"),

	SOLARTHERM_16("solartherm_16.png");

	private final String fileName;

	private Icon(String fileName) {
		this.fileName = fileName;
	}

	String getFileName() {
		return fileName;
	}

	public ImageDescriptor des() {
		return ImageManager.getImageDescriptor(this);
	}

	public Image img() {
		return ImageManager.getImage(this);
	}

}
