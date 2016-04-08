package sophena.rcp.editors.basedata.buildings;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.Viewer;

import sophena.model.BuildingState;

/**
 * We take an own content provider in order to react on input changes which may
 * change the order of the building states and their display in the label
 * provider.
 */
class TableContent extends ArrayContentProvider {

	private TableLabel label;

	public TableContent(TableLabel label) {
		this.label = label;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void inputChanged(Viewer viewer, Object oldIn, Object newIn) {
		if (!(newIn instanceof List))
			return;
		List<BuildingState> list = (List<BuildingState>) newIn;
		label.index(list);
		super.inputChanged(viewer, oldIn, newIn);
	}
}
