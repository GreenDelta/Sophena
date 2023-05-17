package sophena.io.excel.consumers;

enum Field {

	NAME(0),
	DESCRIPTION(1),
	BUILDING_TYPE(2),
	BUILDING_STATE(3),
	TYPE(4),

	HEATING_LOAD(5),
	FUEL(6),
	FUEL_AMOUNT(7),
	FUEL_UNIT(8),
	WATER_FRACTION(9),
	EFFICIENCY_RATE(10),
	UTILIZATION_RATE(11),

	LOCATION(12),
	STREET(13),
	ZIP_CODE(14),
	CITY(15),
	LATITUDE(16),
	LONGITUDE(17);

	/**
	 *
	 */
	final int column;

	Field(int column) {
		this.column = column;
	}

}
