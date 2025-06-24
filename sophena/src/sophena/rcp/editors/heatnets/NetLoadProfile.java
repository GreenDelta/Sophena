package sophena.rcp.editors.heatnets;

import sophena.calc.ProjectLoad;
import sophena.model.LoadProfile;
import sophena.model.Project;
import sophena.model.Stats;

class NetLoadProfile {

	private NetLoadProfile() {
	}

	static LoadProfile get(Project project) {
		LoadProfile profile = new LoadProfile();
		profile.staticData = ProjectLoad.getNetLoadCurve(project);
		profile.dynamicData = new double[Stats.HOURS];
		return profile;
	}

}
