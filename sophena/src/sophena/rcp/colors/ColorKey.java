package sophena.rcp.colors;

import java.util.Objects;
import java.util.Optional;

/**
 * Defines the keys under which custom colors are stored in a configuration.
 */
public enum ColorKey {

	BUFFER_TANK("bufferTankColor"),

	HEATING_OIL("heatingOilColor"),

	NATURAL_GAS("naturalGasColor");

	private final String key;

	ColorKey(String key) {
		this.key = key;
	}

	@Override
	public String toString() {
		return key;
	}

	public static Optional<ColorKey> fromString(String key) {
		for (var k : values()) {
			if (Objects.equals(key, k.key))
				return Optional.of(k);
		}
		return Optional.empty();
	}
}
