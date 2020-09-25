package sophena.rcp;

import sophena.model.AbstractProduct;
import sophena.model.Boiler;
import sophena.model.BufferTank;
import sophena.model.FlueGasCleaning;
import sophena.model.HeatRecovery;
import sophena.model.Pipe;
import sophena.model.TransferStation;
import sophena.utils.Num;

public class SearchLabel {

	private SearchLabel() {
	}

	public static String forProduct(AbstractProduct product) {
		if (product == null)
			return "";
		String label = "";
		if (product.manufacturer != null) {
			label += product.manufacturer.name + " \u00B7 ";
		}
		label += product.name;
		return label;
	}

	public static String forBoiler(Boiler b) {
		String power = b.isCoGenPlant
				? Num.intStr(b.maxPowerElectric)
				: Num.intStr(b.maxPower);
		return forProduct(b, power + " kW");
	}

	public static String forHeatRecovery(HeatRecovery hrc) {
		String power = Num.intStr(hrc.producerPower);
		String label = forProduct(hrc, power + " kW");
		label += " \u00B7 " + hrc.heatRecoveryType + " \u00B7 ";
		label += hrc.fuel;
		return label;
	}

	public static String forFlueGasCleaning(FlueGasCleaning fgc) {
		String power = Num.intStr(fgc.maxProducerPower);
		return forProduct(fgc, power + " kW");
	}

	public static String forBufferTank(BufferTank bt) {
		String volume = Num.intStr(bt.volume);
		return forProduct(bt, volume + " l");
	}

	public static String forPipe(Pipe pipe) {
		var type = pipe.pipeType != null
				? pipe.pipeType.name()
				: "?";
		var diameter = Num.intStr(pipe.outerDiameter) + " mm";
		return type + " \u00B7 " + forProduct(pipe, diameter);
	}

	public static String forTransferStation(TransferStation ts) {
		var capacity = Num.intStr(ts.outputCapacity);
		return forProduct(ts, capacity + " kW");
	}

	private static String forProduct(AbstractProduct product, String keyFigure) {
		if (product == null)
			return "";
		String label = keyFigure;
		if (product.manufacturer != null) {
			label += " \u00B7 " + product.manufacturer.name;
		}
		label += " \u00B7 " + product.name;
		return label;
	}
}
