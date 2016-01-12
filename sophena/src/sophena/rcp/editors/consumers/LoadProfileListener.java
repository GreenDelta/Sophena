package sophena.rcp.editors.consumers;

import sophena.model.LoadProfile;

interface LoadProfileListener {

	void update(LoadProfile profile, double[] totals, double total);

}
