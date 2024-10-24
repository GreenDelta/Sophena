package sophena.rcp.editors.results;

import sophena.model.Producer;
import sophena.model.Project;
import sophena.rcp.utils.MsgBox;

public final class CalculationCheck {

	private CalculationCheck() {
	}

	public static boolean canCalculate(Iterable<Project> it) {
		if (it == null)
			return false;
		int n = 0;
		for (Project p : it) {
			n++;
			if (!canCalculate(p))
				return false;
		}
		return n > 0;
	}

	public static boolean canCalculate(Project p) {
		if (p == null)
			return false;
		for (Producer producer : p.producers) {
			if (producer.disabled)
				continue;
			Double ur = producer.utilisationRate;
			if (ur != null && ur <= 0) {
				MsgBox.error("Plausibilitätsfehler",
						"Der Nutzungsgrad eines"
								+ " Wärmeerzeugers (" + producer.name
								+ ") ist \u2264 0.");
				return false;
			}
		}
		return true;
	}

}
