package sophena.rcp.navigation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.Icon;

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
		for (SubFolderType type : getChildTypes()) {
			SubFolderElement se = new SubFolderElement(type, this);
			childs.add(se);
		}
		return childs;
	}

	private SubFolderType[] getChildTypes() {
		ProjectDescriptor p = content;
		if (p == null)
			return new SubFolderType[0];
		SubFolderType[] types = new SubFolderType[5];
		types[0] = SubFolderType.CONSUMPTION;
		types[1] = SubFolderType.PRODUCTION;
		types[2] = SubFolderType.DISTRIBUTION;
		types[3] = SubFolderType.COSTS;
		types[4] = SubFolderType.RESULTS;
		return types;
	}

	@Override
	public void update() {
		if (childs == null)
			return;
		for (NavigationElement child : childs)
			child.update();
	}

	@Override
	public Image getImage() {
		return Icon.PROJECT_16.img();
	}

}
