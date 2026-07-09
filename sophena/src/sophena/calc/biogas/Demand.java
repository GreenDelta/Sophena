package sophena.calc.biogas;

public enum Demand {

	FULL(1.0),

	RAMP(0.125),

	FULL_RAMP(1.125);

	private final double factor;

	Demand(double factor) {
		this.factor = factor;
	}

	public double factor() {
		return factor;
	}

	public static double factorOf(Demand demand) {
		return demand != null ? demand.factor : 1.0;
	}

}
