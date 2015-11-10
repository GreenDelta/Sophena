package sophena.math;

public class FullLoadHours {

	private double generatedHeat = 0; // kWh
	private double boilerPower = 0; // kW

	private FullLoadHours() {
	}

	public static FullLoadHours boilerPower_kW(double boilerPower) {
		FullLoadHours flh = new FullLoadHours();
		flh.boilerPower = boilerPower;
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
