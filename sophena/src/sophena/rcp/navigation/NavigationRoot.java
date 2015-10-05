package sophena.rcp.navigation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import sophena.db.daos.ProjectDao;
import sophena.model.descriptors.ProjectDescriptor;
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
		ProjectDao dao = new ProjectDao(App.getDb());
		List<ProjectDescriptor> dbContent = dao.getDescriptors();
		ChildSync.sync(childs, dbContent,
				(d) -> new ProjectElement(this, d));
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
