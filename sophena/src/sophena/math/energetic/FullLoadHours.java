package sophena.math.energetic;

public class FullLoadHours {

	private final double boilerPower;
	private double generatedHeat = 0;

	private FullLoadHours(double boilerPower) {
		this.boilerPower = boilerPower;
	}

	public static FullLoadHours boilerPower_kW(double boilerPower) {
		FullLoadHours flh = new FullLoadHours(boilerPower);
		return flh;
	}

	public FullLoadHours generatedHeat_kWh(double generatedHeat) {
		this.generatedHeat = generatedHeat;
		return this;
	}

	public double get_h() {
		if (boilerPower <= 0)
			return 0;
		return generatedHeat / boilerPower;
	}

}
