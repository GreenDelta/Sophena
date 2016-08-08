package sophena.rcp.editors.consumers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import sophena.model.Fuel;

class FuelSorter {

	private static final List<String> ordered = Arrays.asList(new String[] {
			"Heizöl", "Flüssiggas", "Erdgas", "Strom", "Pellets", "Fichte", "Kiefer", "Buche", "Eiche", "Pappel",
			"Weichholz", "Hartholz", "Mischung (70% Wh, 30% Hh)", "Biogas", "Biomethan"
	});

	static void sort(List<Fuel> fuels) {
		Collections.sort(fuels, (f1, f2) -> {
			int order1 = ordered.indexOf(f1.name);
			if (order1 == -1) {
				order1 = Integer.MAX_VALUE;
			}
			int order2 = ordered.indexOf(f2.name);
			if (order2 == -1) {
				order2 = Integer.MAX_VALUE;
			}
			return Integer.compare(order1, order2);
		});
	}

}