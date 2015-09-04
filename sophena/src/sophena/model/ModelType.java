package sophena.model;

public enum ModelType {

	BOILER(Boiler.class),

	BUFFER(BufferTank.class),

	BUILDING_STATE(BuildingState.class),

	CONSUMER(Consumer.class),

	COST_SETTINGS(CostSettings.class),

	FUEL(Fuel.class),

	PIPE(Pipe.class),

	PRODUCER(Producer.class),

	PRODUCT_GROUP(Product.class),

	PROJECT(Project.class),

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
