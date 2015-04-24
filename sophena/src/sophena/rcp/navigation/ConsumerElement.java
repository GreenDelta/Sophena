package sophena.rcp.navigation;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import sophena.model.descriptors.ConsumerDescriptor;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.Images;

public class ConsumerElement extends ContentElement<ConsumerDescriptor> {

	public ConsumerElement(NavigationElement parent, ConsumerDescriptor d) {
		super(parent, d);
	}

	public ProjectDescriptor getProject() {
		if (getParent() instanceof ProjectElement)
			return ((ProjectElement) getParent()).getDescriptor();
		else
			return null;
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
