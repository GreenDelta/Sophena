package sophena.rcp.navigation;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import sophena.model.descriptors.ProducerDescriptor;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.Images;

public class ProducerElement extends ContentElement<ProducerDescriptor> {

	public ProducerElement(NavigationElement parent, ProducerDescriptor d) {
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
		return Images.PRODUCER_16.img();
	}

	@Override
	public void update() {
	}
}
