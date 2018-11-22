package sophena.rcp.navigation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import sophena.db.daos.ProjectFolderDao;
import sophena.model.ModelType;
import sophena.rcp.App;

public class NavigationRoot implements NavigationElement {

	private List<NavigationElement> childs;

	@Override
	public List<NavigationElement> getChilds() {
		if (childs != null)
			return childs;
		childs = new ArrayList<>();
		update();
		return childs;
	}

	@Override
	public void update() {
		if (childs == null)
			return;
		ProjectFolderDao dao = new ProjectFolderDao(App.getDb());
		ChildSync.sync(childs, dao.getAll(), ModelType.PROJECT_FOLDER,
				d -> new FolderElement(this, d));
		ChildSync.sync(childs, dao.rootProjects(), ModelType.PROJECT,
				d -> new ProjectElement(this, d));
	}

	@Override
	public NavigationElement getParent() {
		return null;
	}

	@Override
	public String getLabel() {
		return null;
	}

	@Override
	public int compareTo(NavigationElement other) {
		return 0;
	}

	@Override
	public Image getImage() {
		return null;
	}

}
