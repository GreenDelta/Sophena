package sophena.rcp.navigation;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import sophena.model.descriptors.ConsumerDescriptor;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.Icon;

public class ConsumerElement extends ContentElement<ConsumerDescriptor> {

	public ConsumerElement(NavigationElement parent, ConsumerDescriptor d) {
		super(parent, d);
	}

	public ProjectDescriptor getProject() {
		NavigationElement parent = getParent();
		if (parent instanceof FolderElement)
			return ((FolderElement) parent).getProject();
		else
			return null;
	}

	@Override
	public List<NavigationElement> getChilds() {
		return Collections.emptyList();
	}

	@Override
	public Image getImage() {
		return Icon.CONSUMER_16.img();
	}

	@Override
	public void update() {
	}
}
