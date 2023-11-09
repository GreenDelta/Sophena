package sophena.rcp.colors;

import java.util.Objects;
import java.util.Optional;

/**
 * Defines the keys under which custom colors are stored in a configuration.
 */
public enum ColorKey {

	// common colors
	BUFFER_TANK("bufferTank"),
	LOAD_DYNAMIC("loadDynamic"),
	LOAD_STATIC("loadStatic"),
	PRODUCER_PROFILE("producerProfile"),
	UNCOVERED_LOAD("uncoveredLoad"),

	// emission charts (CO2eq.)
	EMISSIONS("emissions"),
	EMISSIONS_OIL("emissionsOil"),
	EMISSIONS_GAS("emissionsGas"),

	// energy usage
	USED_HEAT("usedHeat"),
	PRODUCED_ELECTRICITY("producedElectricity"),
	LOSSES_CONVERSION("lossesConversion"),
	LOSSES_BUFFER("lossesBuffer"),
	LOSSES_DISTRIBUTION("lossesDistribution")
	;

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
