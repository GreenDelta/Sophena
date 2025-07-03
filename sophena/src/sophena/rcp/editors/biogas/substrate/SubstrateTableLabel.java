package sophena.rcp.editors.biogas.substrate;

import sophena.model.BiogasSubstrate;
import sophena.rcp.editors.basedata.BaseTableLabel;
import sophena.utils.Num;

class SubstrateTableLabel extends BaseTableLabel {

	@Override
	public String getColumnText(Object element, int col) {
		if (!(element instanceof BiogasSubstrate substrate))
			return null;
		return switch (col) {
			case 0 -> substrate.name;
			case 1 -> Num.str(substrate.dryMatter);
			case 2 -> Num.str(substrate.organicDryMatter);
			case 3 -> Num.str(substrate.biogasProduction);
			case 4 -> Num.str(substrate.methaneContent);
			case 5 -> Num.str(substrate.co2Emissions);
			default -> null;
		};
	}
}
