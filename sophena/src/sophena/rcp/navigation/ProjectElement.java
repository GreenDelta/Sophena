package sophena.rcp.navigation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import sophena.model.Project;
import sophena.rcp.Images;
import sophena.rcp.utils.Strings;

public class ProjectElement implements NavigationElement {

	private NavigationElement parent;
	private Project project;

	public ProjectElement(NavigationElement parent, Project project) {
		this.project = project;
		this.parent = parent;
	}

	@Override
	public List<NavigationElement> getChilds() {
		List<NavigationElement> elems = new ArrayList<>();
		for (FolderType type : getChildTypes()) {
			FolderElement se = new FolderElement(type, this);
			elems.add(se);
		}
		return elems;
	}

	private FolderType[] getChildTypes() {
		if (project == null)
			return new FolderType[0];
		int count = project.isVariant() ? 5 : 6;
		FolderType[] types = new FolderType[count];
		types[0] = FolderType.CONSUMPTION;
		types[1] = FolderType.PRODUCTION;
		types[2] = FolderType.DISTRIBUTION;
		types[3] = FolderType.COSTS;
		types[4] = FolderType.ENERGY_RESULT;
		if (!project.isVariant())
			types[5] = FolderType.VARIANTS;
		return types;
	}

	@Override
	public NavigationElement getParent() {
		return parent;
	}

	@Override
	public String getLabel() {
		return project == null ? "#no name" : project.getName();
	}

	@Override
	public int compareTo(NavigationElement other) {
		if (!(other instanceof ProjectElement))
			return 0;
		Project otherProject = ((ProjectElement) other).project;
		if (this.project == null || otherProject == null)
			return 0;
		else
			return Strings.compare(project.getName(), otherProject.getName());
	}

	@Override
	public Object getContent() {
		return project;
	}

	public Project getProject() {
		return project;
	}

	@Override
	public void update() {
	}

	@Override
	public Image getImage() {
		return Images.PROJECT_16.img();
	}

}
