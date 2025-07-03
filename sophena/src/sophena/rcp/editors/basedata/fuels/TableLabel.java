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
			if (!(element instanceof Fuel f))
				return null;
			return switch (col) {
				case 0 -> f.name;
				case 1 -> Num.str(f.calorificValue) + " kWh/" + f.unit;
				case 2 -> Num.str(f.co2Emissions) + " g CO2 äq./kWh";
				case 3 -> Num.str(f.primaryEnergyFactor);
				case 4 -> f.ashContent == 0d
						? null
						: Num.str(f.ashContent) + " %";
				default -> null;
			};
		}
	}

	private static class WoodLabel extends BaseTableLabel {

		@Override
		public String getColumnText(Object element, int col) {
			if (!(element instanceof Fuel f))
				return null;
			return switch (col) {
				case 0 -> f.name;
				case 1 -> Num.str(f.density) + " kg/fm";
				case 2 -> Num.str(f.calorificValue) + " kWh/" + f.unit;
				case 3 -> Num.str(f.co2Emissions) + " g CO2 äq./kWh";
				case 4 -> Num.str(f.primaryEnergyFactor);
				case 5 -> f.ashContent == 0d
						? null
						: Num.str(f.ashContent) + " %";
				default -> null;
			};
		}
	}

}
