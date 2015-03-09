package sophena.rcp.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ProjectDao;
import sophena.model.Project;
import sophena.rcp.App;

public class NavigationRoot implements NavigationElement {

	private List<NavigationElement> childs;

	@Override
	public List<NavigationElement> getChilds() {
		if (childs != null)
			return childs;
		try {
			ProjectDao dao = new ProjectDao(App.getDb());
			childs = new ArrayList<>();
			for (Project p : dao.getAll())
				childs.add(new ProjectElement(this, p));
			return childs;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to get projects from database", e);
			return Collections.emptyList();
		}
	}

	@Override
	public void update() {
		childs = null;
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
	public Object getContent() {
		return this;
	}

	@Override
	public Image getImage() {
		return null;
	}

}
