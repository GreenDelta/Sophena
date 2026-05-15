package sophena.rcp;

import sophena.model.AbstractProduct;
import sophena.model.Boiler;
import sophena.model.BufferTank;
import sophena.model.FlueGasCleaning;
import sophena.model.HeatRecovery;
import sophena.model.Pipe;
import sophena.model.TransferStation;
import sophena.utils.Num;

public final class ProductLabel {

	private ProductLabel() {
	}

	public static String of(AbstractProduct product) {
		return switch (product) {
			case Boiler boiler -> ofBoiler(boiler);
			case HeatRecovery recovery -> ofHeatRecovery(recovery);
			case FlueGasCleaning cleaning -> ofFlueGasCleaning(cleaning);
			case BufferTank bufferTank -> ofBufferTank(bufferTank);
			case Pipe pipe -> ofPipe(pipe);
			case TransferStation station -> ofTransferStation(station);
			case null -> "";
			default -> ofProduct(product);
		};
	}

	private static String ofProduct(AbstractProduct product) {
		String label = "";
		if (product.manufacturer != null) {
			label += product.manufacturer.name + " · ";
		}
		label += product.name;
		return label;
	}

	private static String ofBoiler(Boiler b) {
		String power = b.isCoGenPlant
				? Num.intStr(b.maxPowerElectric)
				: Num.intStr(b.maxPower);
		return withKeyFigure(b, power + " kW");
	}

	private static String ofHeatRecovery(HeatRecovery hrc) {
		String power = Num.intStr(hrc.producerPower);
		String label = withKeyFigure(hrc, power + " kW");
		label += " · " + hrc.heatRecoveryType + " · ";
		label += hrc.fuel;
		return label;
	}

	private static String ofFlueGasCleaning(FlueGasCleaning fgc) {
		String power = Num.intStr(fgc.maxProducerPower);
		return withKeyFigure(fgc, power + " kW");
	}

	private static String ofBufferTank(BufferTank bt) {
		String volume = Num.intStr(bt.volume);
		return withKeyFigure(bt, volume + " l");
	}

	private static String ofPipe(Pipe pipe) {
		var type = pipe.pipeType != null
				? pipe.pipeType.name()
				: "?";
		var diameter = Num.intStr(pipe.outerDiameter) + " mm";
		return type + " · " + withKeyFigure(pipe, diameter);
	}

	private static String ofTransferStation(TransferStation ts) {
		var capacity = Num.intStr(ts.outputCapacity);
		return withKeyFigure(ts, capacity + " kW");
	}

	private static String withKeyFigure(AbstractProduct product, String keyFigure) {
		String label = keyFigure;
		if (product.manufacturer != null) {
			label += " · " + product.manufacturer.name;
		}
		label += " · " + product.name;
		return label;
	}
}
