package sophena.calc.biogas.eblocks;

import java.util.Arrays;

import org.jspecify.annotations.NullMarked;

import sophena.model.biogas.BiogasPlant;

/// The `n` hours of a window with the highest price value.
///
/// The marked hours are only used to decide whether a block can be extended to
/// the left or to the right (`EblockSearch.extend`). They do not decide where
/// a block starts.
@NullMarked
record PriceMarks(int startHour, boolean[] marked) {

	/// Marks the `n` hours with the highest price value in `[t_0, t_e)` of the
	/// window (see `Prices.valueOf`). When two hours have the same price, the
	/// earlier hour is marked first. When the window contains less than `n`
	/// hours, all of its hours are marked.
	static PriceMarks of(BiogasPlant plant, Window window, int n) {
		int from = window.startHour();
		int length = Math.max(0, window.emptyHour() - from);
		var marked = new boolean[length];
		int count = Math.min(n, length);
		if (count <= 0)
			return new PriceMarks(from, marked);

		var hours = new Integer[length];
		for (int i = 0; i < length; i++) {
			hours[i] = from + i;
		}
		Arrays.sort(hours, (h1, h2) -> {
			int cmp = Double.compare(
				Prices.valueOf(plant, h2),
				Prices.valueOf(plant, h1));
			return cmp != 0
				? cmp
				: Integer.compare(h1, h2);
		});
		for (int i = 0; i < count; i++) {
			marked[hours[i] - from] = true;
		}
		return new PriceMarks(from, marked);
	}

	/// `true` when the given hour is one of the marked price optimal hours.
	/// Hours outside of the window are never marked.
	boolean contains(int hour) {
		int i = hour - startHour;
		return i >= 0 && i < marked.length && marked[i];
	}
}
