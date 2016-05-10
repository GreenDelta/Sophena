package sophena.rcp.navigation.actions;

import java.util.List;
import java.util.Objects;

import sophena.model.AbstractEntity;
import sophena.model.descriptors.Descriptor;

class Util {

	static <T extends AbstractEntity> T find(List<T> list, Descriptor d) {
		if (list == null || d == null)
			return null;
		for (T e : list) {
			if (Objects.equals(e.id, d.id))
				return e;
		}
		return null;
	}

}
