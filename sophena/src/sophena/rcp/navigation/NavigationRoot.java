package sophena.rcp.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sophena.db.daos.ProjectDao;
import sophena.model.Project;
import sophena.rcp.App;

public class NavigationRoot implements NavigationElement {

	@Override
	public List<NavigationElement> getChilds() {
		try {
			ProjectDao dao = new ProjectDao(App.getDb());
			List<NavigationElement> list = new ArrayList<>();
			for (Project p : dao.getAll()) {
				list.add(new ProjectElement(p));
			}
			return list;
		} catch (Exception e) {
			return Collections.emptyList();
		}
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

}
