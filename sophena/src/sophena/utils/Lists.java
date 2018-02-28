package sophena.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sophena.model.AbstractEntity;
import sophena.rcp.utils.Strings;

public class Lists {

	private Lists() {
	}

	/**
	 * We often need this to handle JPA synchronization things after updates.
	 */
	public static <T extends AbstractEntity> T find(T t, List<T> list) {
		if (t == null || nullOrEmpty(list))
			return null;
		for (T entry : list) {
			if (entry == null)
				continue;
			if (Strings.nullOrEqual(entry.id, t.id))
				return entry;
		}
		return null;
	}

	/**
	 * Same as {@link #find(AbstractEntity, List)} but for all elements that are
	 * provided as first argument ~> calculates the intersection of both lists
	 * with the elements from the second list as content.
	 */
	public static <T extends AbstractEntity> List<T> findAll(List<T> list,
			List<T> syncList) {
		if (nullOrEmpty(list) || nullOrEmpty(syncList))
			return Collections.emptyList();
		List<T> result = new ArrayList<>();
		for (T t : list) {
			T entry = find(t, syncList);
			if (entry != null) {
				result.add(entry);
			}
		}
		return result;
	}

	public static boolean nullOrEmpty(List<?> list) {
		return list == null || list.isEmpty();
	}
}
