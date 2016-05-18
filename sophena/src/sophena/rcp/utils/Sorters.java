package sophena.rcp.utils;

import java.util.Collections;
import java.util.List;

import sophena.model.AbstractProduct;
import sophena.model.BaseDataEntity;
import sophena.model.Pipe;
import sophena.model.PipeType;
import sophena.model.RootEntity;

public class Sorters {

	private Sorters() {
	}

	public static <T extends RootEntity> void byName(List<T> list) {
		if (list == null)
			return;
		Collections.sort(list, (o1, o2) -> {
			if (o1 == null && o2 == null)
				return 0;
			if (o1 == null || o2 == null)
				return o1 == null ? -1 : 1;
			return Strings.compare(o1.name, o2.name);

		});
	}

	public static void pipes(List<Pipe> pipes) {
		if (pipes == null)
			return;
		Collections.sort(pipes, (p1, p2) -> {
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
		Collections.sort(list, (o1, o2) -> {
			if (o1 == null && o2 == null)
				return 0;
			if (o1 == null || o2 == null)
				return o1 == null ? -1 : 1;
			if (o1.isProtected != o2.isProtected)
				return o1.isProtected ? -1 : 1;
			return Strings.compare(o1.name, o2.name);
		});
	}

}
