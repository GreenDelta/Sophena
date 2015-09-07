package sophena.db.daos;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import sophena.db.Database;
import sophena.model.Boiler;

public class BoilerDao extends RootEntityDao<Boiler> {

	public BoilerDao(Database db) {
		super(Boiler.class, db);
	}

	public List<Boiler> getCoGenPlants() {
		return getAll(true);
	}

	public List<Boiler> getBoilers() {
		return getAll(false);
	}

	private List<Boiler> getAll(boolean coGen) {
		EntityManager em = createManager();
		try {
			String jpql = "SELECT b FROM Boiler b where b.isCoGenPlant = :coGen";
			TypedQuery<Boiler> query = em.createQuery(jpql, Boiler.class);
			query.setParameter("coGen", coGen);
			return query.getResultList();
		} catch (Exception e) {
			log.error("failed to get all boiles coGen=" + coGen, e);
			return Collections.emptyList();
		} finally {
			em.close();
		}
	}
}
