package sophena.model;

public enum ModelType {

	BOILER(Boiler.class),

	CONSUMER(Consumer.class),

	FUEL(Fuel.class),

	PRODUCER(Producer.class),

	PROJECT(Project.class),

	WEATHER_STATION(WeatherStation.class);

	private final Class<? extends RootEntity> modelClass;

	ModelType(Class<? extends RootEntity> modelClass) {
		this.modelClass = modelClass;
	}

	public Class<? extends RootEntity> getModelClass() {
		return modelClass;
	}

	public static ModelType forModelClass(Class<?> type) {
		if(type == null)
			return null;
		for(ModelType t : values()) {
			if(t.modelClass.equals(type))
				return t;
		}
		return null;
	}
}
