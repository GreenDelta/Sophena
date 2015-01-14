package sophena.rcp;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public enum Images {

	CONSUMER_16("consumer.png"),

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
