package sophena.rcp.navigation;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import sophena.model.descriptors.CleaningDescriptor;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.Icon;

public class CleaningElement extends ContentElement<CleaningDescriptor> {

	public CleaningElement(NavigationElement parent, CleaningDescriptor d) {
		super(parent, d);
	}

	public ProjectDescriptor getProject() {
		NavigationElement parent = getParent();
		if (parent instanceof SubFolderElement)
			return ((SubFolderElement) parent).getProject();
		else
			return null;
	}

	@Override
	public List<NavigationElement> getChilds() {
		return Collections.emptyList();
	}

	@Override
	public Image getImage() {
		return Icon.FLUE_GAS_16.img();
	}

	@Override
	public void update() {
	}
}
