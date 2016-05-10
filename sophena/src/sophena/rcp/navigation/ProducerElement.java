package sophena.rcp.navigation;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import sophena.model.descriptors.ProducerDescriptor;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.Icon;

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
		return Icon.PRODUCER_16.img();
	}

	@Override
	public void update() {
	}

	@Override
	public String getLabel() {
		ProducerDescriptor d = getDescriptor();
		if (d == null || d.name == null)
			return super.getLabel();
		return d.rank + ". " + d.name;
	}
}
