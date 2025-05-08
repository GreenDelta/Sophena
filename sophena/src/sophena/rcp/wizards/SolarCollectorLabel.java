package sophena.rcp.wizards;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import sophena.model.SolarCollector;
import sophena.rcp.Icon;
import sophena.utils.Num;

public class SolarCollectorLabel extends LabelProvider
		implements ITableLabelProvider {
	
	@Override
	public Image getColumnImage(Object elem, int col) {
		return null; //col == 0 ? Icon.BOILER_16.img() : null;
	}
	
	@Override
	public String getColumnText(Object elem, int col) {
		if (!(elem instanceof SolarCollector))
			return null;
		SolarCollector s = (SolarCollector) elem;
		switch (col) {
		case 0:
			return s.manufacturer != null
					? s.manufacturer.name
					: null;
		case 1:
			return Num.str(s.collectorArea) + " m2";
		case 2:
			return s.name;
		default:
			return null;
		}
	}
}