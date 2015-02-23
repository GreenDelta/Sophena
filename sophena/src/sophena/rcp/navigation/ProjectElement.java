package sophena.rcp.navigation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import sophena.model.Project;
import sophena.rcp.Images;
import sophena.rcp.utils.Strings;

public class ProjectElement implements NavigationElement {

	private Project project;

	public ProjectElement(Project project) {
		this.project = project;
	}

	@Override
	public List<NavigationElement> getChilds() {
		int[] types = {
				StructureElement.PRODUCTION,
				StructureElement.DISTRIBUTION,
				StructureElement.CONSUMPTION,
				StructureElement.COSTS };
		List<NavigationElement> elems = new ArrayList<>();
		for (int type : types) {
			StructureElement se = new StructureElement(type, this);
			elems.add(se);
		}
		return elems;
	}

	@Override
	public NavigationElement getParent() {
		return null;
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
