package sophena.db.daos;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManager;
import sophena.db.Database;
import sophena.model.AbstractEntity;

public class Dao<T extends AbstractEntity> {

	protected Logger log = LoggerFactory.getLogger(getClass());

	protected Database db;
	private final Class<T> type;

	public Dao(Class<T> type, Database db) {
		this.type = type;
		this.db = db;
	}

	protected Class<T> getType() {
		return type;
	}

	public boolean contains(String id) {
		return db.contains(type, id);
	}

	public T get(String id) {
		return db.get(type, id);
	}

	public List<T> getAll(List<String> ids) {
		return db.getAll(type, ids);
	}

	public List<T> getAll() {
		return db.getAll(type);
	}

	public T insert(T entity) {
		return db.insert(entity);
	}

	public T update(T entity) {
		return db.update(entity);
	}

	public void delete(T entity) {
		db.delete(entity);
	}

	protected EntityManager createManager() {
		return db.getEntityFactory().createEntityManager();
	}

}
