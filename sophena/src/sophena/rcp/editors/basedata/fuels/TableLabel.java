package sophena.rcp.editors.basedata.fuels;

import sophena.model.Fuel;
import sophena.rcp.editors.basedata.BaseTableLabel;
import sophena.utils.Num;

class TableLabel {

	private TableLabel() {
	}

	static BaseTableLabel getForNonWood() {
		return new NonWoodLabel();
	}

	static BaseTableLabel getForWood() {
		return new WoodLabel();
	}

	private static class NonWoodLabel extends BaseTableLabel {

		@Override
		public String getColumnText(Object element, int col) {
			if (!(element instanceof Fuel))
				return null;
			Fuel f = (Fuel) element;
			switch (col) {
			case 0:
				return f.name;
			case 1:
				return Num.str(f.calorificValue)
						+ " kWh/" + f.unit;
			case 2:
				return Num.str(f.co2Emissions) + " g CO2 äq./kWh";
			case 3:
				return Num.str(f.primaryEnergyFactor);
			case 4:
				if (f.ashContent == 0d)
					return null;
				return Num.str(f.ashContent) + " %";
			default:
				return null;
			}
		}
	}

	private static class WoodLabel extends BaseTableLabel {

		@Override
		public String getColumnText(Object element, int col) {
			if (!(element instanceof Fuel))
				return null;
			Fuel f = (Fuel) element;
			switch (col) {
			case 0:
				return f.name;
			case 1:
				return Num.str(f.density) + " kg/fm";
			case 2:
				return Num.str(f.calorificValue)
						+ " kWh/kg atro";
			case 3:
				return Num.str(f.co2Emissions) + " g CO2 äq./kWh";
			case 4:
				return Num.str(f.primaryEnergyFactor);
			case 5:
				if (f.ashContent == 0d)
					return null;
				return Num.str(f.ashContent) + " %";
			default:
				return null;
			}
		}
	}

}
