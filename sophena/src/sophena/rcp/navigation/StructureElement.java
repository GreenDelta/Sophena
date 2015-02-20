package sophena.rcp.navigation;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import sophena.model.Project;
import sophena.rcp.Images;

public class StructureElement implements NavigationElement {

	public static final int DISTRIBUTION = 0;
	public static final int CONSUMPTION = 1;
	public static final int COSTS = 2;

	private final int type;
	private final ProjectElement parent;

	public StructureElement(int type, ProjectElement parent) {
		this.type = type;
		this.parent = parent;
	}

	public int getType() {
		return type;
	}

	public Project getProject() {
		if (parent != null)
			return parent.getProject();
		else
			return null;
	}

	@Override
	public List<NavigationElement> getChilds() {
		return Collections.emptyList();
	}

	@Override
	public NavigationElement getParent() {
		return parent;
	}

	@Override
	public String getLabel() {
		switch (type) {
		case DISTRIBUTION:
			return "#Wärmeverteilung";
		case CONSUMPTION:
			return "#Wärmenutzung";
		case COSTS:
			return "#Kosten";
		default:
			return "#unknown";
		}
	}

	@Override
	public int compareTo(NavigationElement other) {
		if (!(other instanceof StructureElement))
			return 0;
		StructureElement otherElem = (StructureElement) other;
		return Integer.compare(this.type, otherElem.type);
	}

	@Override
	public Object getContent() {
		return this;
	}

	@Override
	public void update() {
		// TODO update project content
	}

	@Override
	public Image getImage() {
		switch (type) {
		case CONSUMPTION:
			return Images.CONSUMER_16.img();
		case COSTS:
			return Images.COSTS_16.img();
		default:
			return null;
		}
	}

}
