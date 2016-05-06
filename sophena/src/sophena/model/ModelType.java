package sophena.model;

public enum ModelType {

	BOILER(Boiler.class),

	BUFFER(BufferTank.class),

	BUILDING_STATE(BuildingState.class),

	CONSUMER(Consumer.class),

	COST_SETTINGS(CostSettings.class),

	FLUE_GAS_CLEANING(FlueGasCleaning.class),

	FUEL(Fuel.class),

	HEAT_RECOVERY(HeatRecovery.class),

	LOAD_PROFILE(LoadProfile.class),

	MANUFACTURER(Manufacturer.class),

	PIPE(Pipe.class),

	PRODUCER(Producer.class),

	PRODUCT_GROUP(ProductGroup.class),

	PRODUCT(Product.class),

	PROJECT(Project.class),

	TRANSFER_STATION(TransferStation.class),

	WEATHER_STATION(WeatherStation.class);

	private final Class<? extends AbstractEntity> modelClass;

	ModelType(Class<? extends AbstractEntity> modelClass) {
		this.modelClass = modelClass;
	}

	public Class<? extends AbstractEntity> getModelClass() {
		return modelClass;
	}

	public static ModelType forModelClass(Class<?> type) {
		if (type == null)
			return null;
		for (ModelType t : values()) {
			if (t.modelClass.equals(type))
				return t;
		}
		return null;
	}
}
