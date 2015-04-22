package sophena.db.daos;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import sophena.db.Database;
import sophena.model.Descriptor;
import sophena.model.ModelType;
import sophena.model.RootEntity;

public class RootEntityDao<T extends RootEntity> extends Dao<T> {

	public RootEntityDao(Class<T> type, Database db) {
		super(type, db);
	}

	@SuppressWarnings("unchecked")
	public List<Descriptor> getDescriptors() {
		String jpql = "SELECT o.id, o.name, o.description FROM "
				+ getType().getSimpleName() + " o";
		List<Descriptor> list = new ArrayList<>();
		EntityManager em = createManager();
		try {
			List<Object[]> results = em.createQuery(jpql).getResultList();
			for (Object[] result : results) {
				Descriptor d = new Descriptor();
				d.setId(str(result[0]));
				d.setName(str(result[1]));
				d.setDescription(str(result[2]));
				d.setType(ModelType.forModelClass(getType()));
				list.add(d);
			}
		} catch (Exception e) {
			log.error("failed to get descriptors for " + getType(), e);
		} finally {
			em.close();
		}
		return list;
	}

	private String str(Object o) {
		if (o == null)
			return null;
		if (o instanceof String)
			return (String) o;
		else
			return o.toString();
	}
}
