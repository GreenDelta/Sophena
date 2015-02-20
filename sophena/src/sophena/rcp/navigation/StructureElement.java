package sophena.rcp.navigation;

import java.util.Collections;
import java.util.List;

class StructureElement implements NavigationElement {

	static final int DISTRIBUTION = 0;
	static final int USAGE = 1;
	static final int COSTS = 2;

	private final int type;
	private final ProjectElement parent;

	public StructureElement(int type, ProjectElement parent) {
		this.type = type;
		this.parent = parent;
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
		case USAGE:
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

}
