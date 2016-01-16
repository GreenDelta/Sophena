package sophena.rcp.utils;

import java.util.Collections;
import java.util.List;

import sophena.model.BaseDataEntity;
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
