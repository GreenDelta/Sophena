package sophena.calc.biogas.eblocks;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record Block(State start, State end) {

	int length() {
		return end.hour() - start.hour();
	}

	@Nullable
	Block tryExtendLeft() {
		var prev = start.previous();
		return prev != null
			? prev.getBlock(length() + 1)
			: null;
	}

	@Nullable
	Block tryExtendRight() {
		return start.getBlock(length() + 1);
	}

}
