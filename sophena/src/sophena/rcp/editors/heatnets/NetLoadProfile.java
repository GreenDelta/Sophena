package sophena.rcp.editors.heatnets;

import sophena.calc.ProjectLoad;
import sophena.model.HeatNet;
import sophena.model.LoadProfile;
import sophena.model.Stats;

class NetLoadProfile {

	private NetLoadProfile() {
	}

	static LoadProfile get(HeatNet net) {
		LoadProfile profile = new LoadProfile();
		profile.staticData = ProjectLoad.getNetLoadCurve(net);
		profile.dynamicData = new double[Stats.HOURS];
		return profile;
	}

}
