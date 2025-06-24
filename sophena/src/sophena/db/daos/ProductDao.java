package sophena.db.daos;

import java.util.Collections;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import sophena.db.Database;
import sophena.model.Product;
import sophena.model.ProductType;

public class ProductDao extends RootEntityDao<Product> {

	public ProductDao(Database db) {
		super(Product.class, db);
	}

	public List<Product> getAllGlobal(ProductType type) {
		if (type == null)
			return Collections.emptyList();
		EntityManager em = createManager();
		try {
			String jpql = "SELECT p FROM Product p where p.projectId is null "
					+ "and p.type = :type";
			TypedQuery<Product> query = em.createQuery(jpql, Product.class);
			query.setParameter("type", type);
			return query.getResultList();
		} catch (Exception e) {
			log.error("failed to get products of type " + type, e);
			return Collections.emptyList();
		} finally {
			em.close();
		}
	}
}
