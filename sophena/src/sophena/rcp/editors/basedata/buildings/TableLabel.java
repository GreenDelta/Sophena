package sophena.rcp.editors.basedata.buildings;

import org.eclipse.swt.graphics.Image;

import sophena.model.BuildingState;
import sophena.rcp.Icon;
import sophena.rcp.Labels;
import sophena.rcp.editors.basedata.BaseTableLable;
import sophena.utils.Num;

class TableLabel extends BaseTableLable {
	@Override
	public Image getColumnImage(Object obj, int col) {
		if (!(obj instanceof BuildingState))
			return null;
		BuildingState s = (BuildingState) obj;
		if (col == 0)
			return s.index == 0 ? Icon.BUILDING_TYPE_16.img() : null;
		if (col == 1)
			return s.isProtected ? Icon.LOCK_16.img() : Icon.EDIT_16.img();
		if (col == 5)
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
			return s.index == 0 ? Labels.get(s.type) : null;
		case 1:
			return s.name;
		case 2:
			return Num.str(s.heatingLimit) + " °C";
		case 3:
			return Num.str(s.waterFraction) + " %";
		case 4:
			return Num.intStr(s.loadHours) + " h";
		default:
			return null;
		}
	}
}
