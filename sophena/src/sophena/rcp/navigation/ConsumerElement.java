package sophena.rcp.navigation;

import java.util.Collections;
import java.util.List;
import org.eclipse.swt.graphics.Image;
import sophena.model.descriptors.ConsumerDescriptor;
import sophena.rcp.Images;

public class ConsumerElement extends  ContentElement<ConsumerDescriptor> {

	public ConsumerElement(NavigationElement parent, ConsumerDescriptor d) {
		super(parent, d);
	}

	@Override
	public List<NavigationElement> getChilds() {
		return Collections.emptyList();
	}

	@Override
	public Image getImage() {
		return Images.CONSUMER_16.img();
	}

	@Override
	public void update() {
	}
}
