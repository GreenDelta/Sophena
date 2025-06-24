package sophena.rcp.utils;

import java.util.Comparator;
import java.util.List;

import sophena.model.AbstractProduct;
import sophena.model.BaseDataEntity;
import sophena.model.Boiler;
import sophena.model.BufferTank;
import sophena.model.FlueGasCleaning;
import sophena.model.HeatPump;
import sophena.model.HeatRecovery;
import sophena.model.Pipe;
import sophena.model.PipeType;
import sophena.model.ProductGroup;
import sophena.model.RootEntity;
import sophena.model.SolarCollector;
import sophena.model.TransferStation;
import sophena.utils.Strings;

public class Sorters {

	private Sorters() {
	}

	public static <T extends RootEntity> Comparator<T> byName() {
		return (o1, o2) -> {
			if (o1 == null && o2 == null)
				return 0;
			if (o1 == null || o2 == null)
				return o1 == null ? -1 : 1;
			return Strings.compare(o1.name, o2.name);
		};
	}

	public static <T extends RootEntity> void byName(List<T> list) {
		if (list == null)
			return;
		list.sort(byName());
	}

	public static void boilers(List<Boiler> boilers) {
		if (boilers == null)
			return;
		boilers.sort((p1, p2) -> {
			int c = byGroup(p1, p2);
			if (c != 0 || p1 == null || p2 == null)
				return c;
			if (p1.isCoGenPlant) {
				if (Math.abs(
						p1.maxPowerElectric - p2.maxPowerElectric) > 1e-6) {
					return Double.compare(p1.maxPowerElectric,
							p2.maxPowerElectric);
				}
			} else {
				if (Math.abs(p1.maxPower - p2.maxPower) > 1e-6) {
					return Double.compare(p1.maxPower, p2.maxPower);
				}
			}
			return byManufacturer(p1, p2);
		});
	}

	public static void pipes(List<Pipe> pipes) {
		if (pipes == null)
			return;
		pipes.sort((p1, p2) -> {
			int c = byGroup(p1, p2);
			if (c != 0 || p1 == null || p2 == null)
				return c;
			if (p1.pipeType != p2.pipeType) {
				if (p1.pipeType == null || p2.pipeType == null)
					return p1.pipeType == null ? 1 : -1;
				return p1.pipeType == PipeType.UNO ? 1 : -1;
			}
			if (Math.abs(p1.outerDiameter - p2.outerDiameter) > 1e-6) {
				return Double.compare(p1.outerDiameter, p2.outerDiameter);
			}
			if (Math.abs(p1.uValue - p2.uValue) > 1e-6) {
				return Double.compare(p1.uValue, p2.uValue);
			}
			return byManufacturer(p1, p2);
		});
	}

	public static void transferStations(List<TransferStation> ts) {
		if (ts == null)
			return;
		ts.sort((p1, p2) -> {
			int c = byGroup(p1, p2);
			if (c != 0 || p1 == null || p2 == null)
				return c;

			if (Math.abs(p1.outputCapacity - p2.outputCapacity) > 1e-6) {
				return Double.compare(p1.outputCapacity, p2.outputCapacity);
			}
			return byManufacturer(p1, p2);
		});
	}

	public static void solarCollectors(List<SolarCollector> sc) {
		if (sc == null)
			return;
		sc.sort((p1, p2) -> {
			int c = byGroup(p1, p2);
			if (c != 0 || p1 == null || p2 == null)
				return c;

			if (Math.abs(p1.collectorArea - p2.collectorArea) > 1e-6) {
				return Double.compare(p1.collectorArea, p2.collectorArea);
			}
			return byManufacturer(p1, p2);
		});
	}
	
	public static void heatPumps(List<HeatPump> hp) {
		if (hp == null)
			return;
		hp.sort((p1, p2) -> {
			int c = byGroup(p1, p2);
			if (c != 0 || p1 == null || p2 == null)
				return c;

			return byManufacturer(p1, p2);
		});
	}
	
	public static void heatRecoveries(List<HeatRecovery> hr) {
		if (hr == null)
			return;
		hr.sort((p1, p2) -> {
			int c = byGroup(p1, p2);
			if (c != 0 || p1 == null || p2 == null)
				return c;
			c = Strings.compare(p1.heatRecoveryType, p2.heatRecoveryType);
			if (c != 0)
				return c;
			if (Math.abs(p1.producerPower - p2.producerPower) > 1e-6) {
				return Double.compare(p1.producerPower, p2.producerPower);
			}
			c = Strings.compare(p1.fuel, p2.fuel);
			return c == 0 ? byManufacturer(p1, p2) : c;
		});
	}

	public static void flueGasCleanings(List<FlueGasCleaning> fgc) {
		if (fgc == null)
			return;
		fgc.sort((p1, p2) -> {
			int c = byGroup(p1, p2);
			if (c != 0 || p1 == null || p2 == null)
				return c;
			if (Math.abs(p1.maxProducerPower - p2.maxProducerPower) > 1e-6) {
				return Double.compare(p1.maxProducerPower, p2.maxProducerPower);
			}
			c = Strings.compare(p1.fuel, p2.fuel);
			if (c != 0)
				return c;
			return byManufacturer(p1, p2);
		});
	}

	public static void buffers(List<BufferTank> buffers) {
		if (buffers == null)
			return;
		buffers.sort((p1, p2) -> {
			int c = byGroup(p1, p2);
			if (c != 0 || p1 == null || p2 == null)
				return c;
			if (Math.abs(p1.volume - p2.volume) > 1e-6) {
				return Double.compare(p1.volume, p2.volume);
			}
			return byManufacturer(p1, p2);
		});
	}

	private static int byGroup(AbstractProduct p1, AbstractProduct p2) {
		if (p1 == null && p2 == null)
			return 0;
		if (p1 == null || p2 == null)
			return p1 == null ? -1 : 1;
		if (p1.group == null && p2.group == null)
			return 0;
		if (p1.group == null || p2.group == null)
			return p1.group == null ? 1 : -1;
		return Strings.compare(p1.group.name, p2.group.name);
	}

	private static int byManufacturer(AbstractProduct p1, AbstractProduct p2) {
		if (p1 == null && p2 == null)
			return 0;
		if (p1 == null || p2 == null)
			return p1 == null ? -1 : 1;
		if (p1.manufacturer == null && p2.manufacturer == null)
			return 0;
		if (p1.manufacturer == null || p2.manufacturer == null)
			return p1.manufacturer == null ? 1 : -1;
		return Strings.compare(p1.manufacturer.name, p2.manufacturer.name);
	}

	/**
	 * Sorts the given list by name but protected data are always inserted
	 * before other data.
	 */
	public static <T extends BaseDataEntity> void sortBaseData(List<T> list) {
		if (list == null)
			return;
		list.sort((o1, o2) -> {
			if (o1 == null && o2 == null)
				return 0;
			if (o1 == null || o2 == null)
				return o1 == null ? -1 : 1;
			if (o1.isProtected != o2.isProtected)
				return o1.isProtected ? -1 : 1;
			return Strings.compare(o1.name, o2.name);
		});
	}

	public static void productGroups(List<ProductGroup> groups) {
		if (groups == null)
			return;
		groups.sort((g1, g2) -> {
			if (g1.type == null || g2.type == null)
				return 0;
			if (g1.type != g2.type)
				return g1.type.ordinal() - g2.type.ordinal();
			if (g1.index != g2.index)
				return g1.index - g2.index;
			return Strings.compare(g1.name, g2.name);
		});
	}
}
