package sophena.rcp.editors.basedata.buildings;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import sophena.model.BuildingState;

class TableSorter extends ViewerComparator {

	@Override
	public int compare(Viewer viewer, Object o1, Object o2) {
		if (!(o1 instanceof BuildingState)
				|| !(o2 instanceof BuildingState))
			return 0;
		BuildingState s1 = (BuildingState) o1;
		BuildingState s2 = (BuildingState) o2;
		if (s2.type == null || s1.type == null)
			return 0;
		if (s1.type != s2.type)
			return s1.type.ordinal() - s2.type.ordinal();
		return s1.index - s2.index;
	}
}
