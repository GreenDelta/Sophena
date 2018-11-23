package sophena.rcp.navigation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import sophena.model.ProjectFolder;
import sophena.rcp.Icon;

public class FolderElement extends ContentElement<ProjectFolder> {

	private List<NavigationElement> childs;

	public FolderElement(NavigationElement parent, ProjectFolder folder) {
		super(parent, folder);
	}

	@Override
	public List<NavigationElement> getChilds() {
		if (childs != null)
			return childs;
		childs = new ArrayList<>();
		// TODO: collect project descriptors in this folder
		return childs;
	}

	@Override
	public Image getImage() {
		return Icon.FOLDER_16.img();
	}

	@Override
	public void update() {
		if (childs == null)
			return;
		for (NavigationElement child : childs) {
			child.update();
		}
	}

}
