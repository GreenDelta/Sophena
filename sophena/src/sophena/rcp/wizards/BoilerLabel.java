package sophena.rcp.wizards;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import sophena.model.Boiler;
import sophena.model.HeatPump;
import sophena.rcp.Icon;
import sophena.utils.Num;

class BoilerLabel extends LabelProvider
		implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object elem, int col) {
		return col == 0 ? Icon.BOILER_16.img() : null;
	}

	@Override
	public String getColumnText(Object elem, int col) {
		if(elem instanceof HeatPump)
		{
			HeatPump p = (HeatPump) elem;
			switch (col) {
			case 0:
				return p.manufacturer != null
						? p.manufacturer.name
						: null;
			case 1:
				return Num.str(p.ratedPower) + " kW";
			case 2:
				return p.name;
			default:
				return null;
			}
		}
		if (!(elem instanceof Boiler))
			return null;
		Boiler b = (Boiler) elem;
		switch (col) {
		case 0:
			return b.manufacturer != null
					? b.manufacturer.name
					: null;
		case 1:
			return b.isCoGenPlant
					? Num.str(b.maxPowerElectric) + " kW el."
					: Num.str(b.maxPower) + " kW";
		case 2:
			return b.name;
		default:
			return null;
		}
	}
}