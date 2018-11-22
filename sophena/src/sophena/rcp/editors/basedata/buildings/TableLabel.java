package sophena.rcp.editors.basedata.buildings;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;

import sophena.Labels;
import sophena.model.BuildingState;
import sophena.model.BuildingType;
import sophena.rcp.Icon;
import sophena.rcp.editors.basedata.BaseTableLabel;
import sophena.utils.Num;

class TableLabel extends BaseTableLabel {

	private Map<BuildingType, Integer> lowestIndex = new HashMap<>();

	/**
	 * We index the building states with the lowest index value to display the
	 * building type label and icon only for the first building state with a
	 * given type.
	 */
	void index(Collection<? extends BuildingState> states) {
		if (states == null)
			return;
		lowestIndex.clear();
		for (BuildingState s : states) {
			if (s.type == null)
				continue;
			Integer lowest = lowestIndex.get(s.type);
			if (lowest == null || s.index < lowest) {
				lowestIndex.put(s.type, s.index);
			}
		}
	}

	private boolean isFirst(BuildingState s) {
		if (s == null || s.type == null)
			return false;
		Integer idx = lowestIndex.get(s.type);
		if (idx == null)
			return false;
		return idx == s.index;
	}

	@Override
	public Image getColumnImage(Object obj, int col) {
		if (!(obj instanceof BuildingState))
			return null;
		BuildingState s = (BuildingState) obj;
		if (col == 0 && isFirst(s))
			return Icon.BUILDING_TYPE_16.img();
		if (col == 1)
			return s.isProtected ? Icon.LOCK_16.img() : Icon.EDIT_16.img();
		if (col == 6)
			return s.isDefault ? Icon.CHECKBOX_CHECKED_16.img()
					: Icon.CHECKBOX_UNCHECKED_16.img();
		else
			return null;
	}

	@Override
	public String getColumnText(Object obj, int col) {
		if (!(obj instanceof BuildingState))
			return null;
		BuildingState s = (BuildingState) obj;
		switch (col) {
		case 0:
			return isFirst(s) ? Labels.get(s.type) : null;
		case 1:
			return Num.intStr(s.index) + ".) " + s.name;
		case 2:
			return Num.str(s.heatingLimit) + " °C";
		case 3:
			return Num.str(s.antifreezingTemperature) + " °C";
		case 4:
			return Num.str(s.waterFraction) + " %";
		case 5:
			return Num.intStr(s.loadHours) + " h";
		default:
			return null;
		}
	}
}
