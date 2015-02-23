package sophena.rcp;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public enum Images {

	ADD_16("add_16.png"),

	CONSUMER_16("consumer_16.png"),

	COSTS_16("costs_16.png"),

	DELETE_16("delete_16.png"),

	EDIT_16("edit_16.png"),

	FILE_16("file_16.png"),

	INFO_16("info_16.png"),

	NEW_16("new_16.png"),

	NEW_PROJECT_16("new_project_16.png"),

	OPEN_16("open_16.png"),

	PROJECT_16("project_16.png"),

	PRODUCER_16("producer.png"),

	PUMP_16("pump.png");

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
