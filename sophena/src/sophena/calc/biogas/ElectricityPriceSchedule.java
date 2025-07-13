package sophena.calc.biogas;

public record ElectricityPriceSchedule(boolean[] flags) {

	public boolean shouldRunAt(int hour) {
		return flags[hour];
	}



}
