package sophena.rcp.editors.consumers;

import sophena.model.LoadProfile;

@FunctionalInterface
interface LoadProfileListener {

	void update(LoadProfile profile, double[] totals, double total);

}
