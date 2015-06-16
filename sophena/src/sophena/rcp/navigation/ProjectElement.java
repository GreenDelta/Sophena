package sophena.rcp.navigation;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.graphics.Image;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.Images;

public class ProjectElement extends ContentElement<ProjectDescriptor> {

	private List<NavigationElement> childs;

	public ProjectElement(NavigationElement parent, ProjectDescriptor project) {
		super(parent, project);
	}

	@Override
	public List<NavigationElement> getChilds() {
		if (childs != null)
			return childs;
		childs = new ArrayList<>();
		for (FolderType type : getChildTypes()) {
			FolderElement se = new FolderElement(type, this);
			childs.add(se);
		}
		return childs;
	}

	private FolderType[] getChildTypes() {
		ProjectDescriptor p = getDescriptor();
		if (p == null)
			return new FolderType[0];
		int count = p.isVariant() ? 5 : 6;
		FolderType[] types = new FolderType[count];
		types[0] = FolderType.CONSUMPTION;
		types[1] = FolderType.PRODUCTION;
		types[2] = FolderType.DISTRIBUTION;
		types[3] = FolderType.COSTS;
		types[4] = FolderType.RESULTS;
		if (!p.isVariant())
			types[5] = FolderType.VARIANTS;
		return types;
	}

	public void update() {
		if (childs == null)
			return;
		for (NavigationElement child : childs)
			child.update();
	}

	@Override
	public Image getImage() {
		return Images.PROJECT_16.img();
	}

}
