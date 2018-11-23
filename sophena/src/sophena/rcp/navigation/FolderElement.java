package sophena.rcp.navigation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import sophena.db.daos.ProjectFolderDao;
import sophena.model.ModelType;
import sophena.model.ProjectFolder;
import sophena.rcp.App;
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
		update();
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
		ProjectFolderDao dao = new ProjectFolderDao(App.getDb());
		ChildSync.sync(
				childs,
				dao.getProjects(content),
				ModelType.PROJECT,
				d -> new ProjectElement(this, d));
	}

}
