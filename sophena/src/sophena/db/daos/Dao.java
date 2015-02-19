package sophena.db.daos;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.Database;

public class Dao<T> {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Database db;
	private Class<T> type;

	public Dao(Class<T> type, Database db) {
		this.type = type;
		this.db = db;
	}

	protected Class<T> getType() {
		return type;
	}

	public T get(String id) {
		log.trace("get {} for id={}", type, id);
		EntityManager entityManager = createManager();
		try {
			T o = entityManager.find(type, id);
			return o;
		} catch (Exception e) {
			log.error("Error while loading " + type + " with id " + id, e);
			return null;
		} finally {
			entityManager.close();
		}
	}

	public List<T> getAll(List<String> ids) {
		EntityManager em = createManager();
		try {
			String jpql = "SELECT o FROM " + type.getSimpleName()
					+ " o WHERE o.id IN :ids";
			TypedQuery<T> query = em.createQuery(jpql, type);
			query.setParameter("ids", ids);
			return query.getResultList();
		} catch (Exception e) {
			log.error("Error while loading " + type + " for ids", e);
			return Collections.emptyList();
		} finally {
			em.close();
		}
	}

	public List<T> getAll() {
		log.debug("Select all for class {}", type);
		EntityManager em = createManager();
		try {
			String jpql = "SELECT o FROM " + type.getSimpleName() + " o";
			TypedQuery<T> query = em.createQuery(jpql, type);
			List<T> results = query.getResultList();
			log.debug("{} results", results.size());
			return results;
		} catch (Exception e) {
			log.error("Error while loading all instances of " + type, e);
			return Collections.emptyList();
		} finally {
			em.close();
		}
	}

	public T insert(T entity) {
		if (entity == null)
			return null;
		EntityManager em = createManager();
		try {
			em.getTransaction().begin();
			em.persist(entity);
			em.getTransaction().commit();
			return entity;
		} catch (Exception e) {
			log.error("Error while inserting " + entity, e);
			return entity;
		} finally {
			em.close();
		}
	}

	public T update(T entity) {
		if (entity == null)
			return null;
		EntityManager em = createManager();
		try {
			em.getTransaction().begin();
			T retval = em.merge(entity);
			em.getTransaction().commit();
			return retval;
		} catch (Exception e) {
			log.error("Error while updating " + entity, e);
			return entity;
		} finally {
			em.close();
		}
	}

	public void delete(T entity) {
		if (entity == null)
			return;
		EntityManager em = createManager();
		try {
			em.getTransaction().begin();
			em.remove(em.merge(entity));
			em.getTransaction().commit();
		} catch (Exception e) {
			log.error("Error while deleting " + entity, e);
		} finally {
			em.close();
		}
	}

	protected EntityManager createManager() {
		return db.getEntityFactory().createEntityManager();
	}

}
