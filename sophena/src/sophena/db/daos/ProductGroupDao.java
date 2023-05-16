package sophena.db.daos;

import java.util.Collections;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import sophena.db.Database;
import sophena.model.ProductGroup;
import sophena.model.ProductType;

public class ProductGroupDao extends RootEntityDao<ProductGroup> {

	public ProductGroupDao(Database db) {
		super(ProductGroup.class, db);
	}

	public List<ProductGroup> getAll(ProductType type) {
		if (type == null)
			return Collections.emptyList();
		EntityManager em = createManager();
		try {
			String jpql = "SELECT p FROM ProductGroup p where p.type = :type";
			TypedQuery<ProductGroup> query = em.createQuery(jpql,
					ProductGroup.class);
			query.setParameter("type", type);
			return query.getResultList();
		} catch (Exception e) {
			log.error("failed to get product groups for type " + type, e);
			return Collections.emptyList();
		} finally {
			em.close();
		}
	}

}
