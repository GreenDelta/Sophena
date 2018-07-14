package sophena.db.daos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sophena.db.Database;
import sophena.model.BuildingState;
import sophena.model.BuildingType;

public class BuildingStateDao extends RootEntityDao<BuildingState> {

	public BuildingStateDao(Database db) {
		super(BuildingState.class, db);
	}

	/**
	 * Get all building types with the given type. The results are sorted so
	 * that they can be used directly in the user interface.
	 */
	public List<BuildingState> getAllWith(BuildingType type) {
		List<BuildingState> all = getAll();
		List<BuildingState> states = new ArrayList<>();
		for (BuildingState s : all) {
			if (s.type != type)
				continue;
			states.add(s);
		}
		Collections.sort(states, (s1, s2) -> s1.index - s2.index);
		return states;
	}

	public static BuildingState getDefault(List<BuildingState> states) {
		if (states == null || states.isEmpty())
			return null;
		for (BuildingState state : states) {
			if (state.isDefault)
				return state;
		}
		return states.get(0);
	}

}
