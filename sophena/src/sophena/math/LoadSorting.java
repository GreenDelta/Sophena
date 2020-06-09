package sophena.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sophena.model.LoadProfile;
import sophena.model.Stats;

/**
 * Creates a sorted copy of a load profile.
 */
public class LoadSorting {

	private LoadProfile orig;

	private LoadSorting(LoadProfile orig) {
		this.orig = orig;
	}

	public static LoadProfile sort(LoadProfile profile) {
		if (profile == null)
			return null;
		if (profile.dynamicData == null)
			profile.dynamicData = new double[Stats.HOURS];
		if (profile.staticData == null)
			profile.staticData = new double[Stats.HOURS];
		return new LoadSorting(profile).sort();
	}

	private LoadProfile sort() {
		double[] totals = orig.calculateTotal();
		List<Item> items = new ArrayList<>();
		for (int i = 0; i < totals.length; i++) {
			Item item = new Item();
			item.origIndex = i;
			item.total = totals[i];
			items.add(item);
		}
		Collections.sort(items);
		LoadProfile sorted = new LoadProfile();
		sorted.dynamicData = new double[Stats.HOURS];
		sorted.staticData = new double[Stats.HOURS];
		for (int i = 0; i < items.size(); i++) {
			int oldIdx = items.get(i).origIndex;
			sorted.dynamicData[i] = orig.dynamicData[oldIdx];
			sorted.staticData[i] = orig.staticData[oldIdx];
		}
		return sorted;
	}

	private class Item implements Comparable<Item> {
		int origIndex;
		double total;

		@Override
		public int compareTo(Item o) {
			if (o == null)
				return -1;
			return Double.compare(o.total, total);
		}
	}

}
