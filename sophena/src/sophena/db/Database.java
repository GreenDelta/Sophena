package sophena.db;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.derby.iapi.jdbc.AutoloadedDriver;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class Database implements Closeable {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final File folder;
	private final String url;

	private EntityManagerFactory entityFactory;
	private HikariDataSource pool;
	private boolean closed = false;

	public Database(File folder) {
		this.folder = folder;
		registerDriver();
		url = "jdbc:derby:" + folder.getAbsolutePath().replace('\\', '/');
		if (shouldCreateNew())
			createNew(url);
		connect(url);
	}

	private void registerDriver() {
		try {
			DriverManager.registerDriver(new AutoloadedDriver());
		} catch (Exception e) {
			throw new RuntimeException("Could not register driver", e);
		}
	}

	private boolean shouldCreateNew() {
		// see the Derby folder specification:
		// http://db.apache.org/derby/docs/10.0/manuals/develop/develop13.html
		if (!folder.exists())
			return true;
		File log = new File(folder, "log");
		if (!log.exists())
			return true;
		File seg0 = new File(folder, "seg0");
		return !seg0.exists();
	}

	private void createNew(String url) {
		log.info("create new database {}", url);
		try {
			Connection con = DriverManager.getConnection(url + ";create=true");
			con.close();
			ScriptRunner runner = new ScriptRunner(this);
			runner.run(getClass().getResourceAsStream("db.sql"), "utf-8");
		} catch (Exception e) {
			log.error("failed to create database", e);
			throw new RuntimeException("Failed to create database", e);
		}
	}

	private void connect(String url) {
		log.trace("connect to database: {}", url);
		Map<Object, Object> map = new HashMap<>();
		map.put("jakarta.persistence.jdbc.url", url);
		map.put("jakarta.persistence.jdbc.driver",
				"org.apache.derby.iapi.jdbc.AutoloadedDriver");
		map.put("eclipselink.classloader", getClass().getClassLoader());
		map.put("eclipselink.target-database", "Derby");
		entityFactory = new PersistenceProvider().createEntityManagerFactory(
				"sophena", map);
		initConnectionPool(url);
	}

	private void initConnectionPool(String url) {
		try {
			pool = new HikariDataSource();
			pool.setJdbcUrl(url);
		} catch (Exception e) {
			log.error("failed to initialize connection pool", e);
			throw new RuntimeException("Could not create a connection", e);
		}
	}

	@Override
	public void close() throws IOException {
		if (closed)
			return;
		log.trace("close database: {}", url);
		if (entityFactory != null && entityFactory.isOpen())
			entityFactory.close();
		if (pool != null)
			pool.close();
		try {
			DriverManager.getConnection(url + ";shutdown=true");
			// single database shutdown throws unexpected
			// error in eclipse APP - close all connections here
			// DriverManager.getConnection("jdbc:derby:;shutdown=true");
			System.gc(); // unload embedded driver for possible restarts
			// see also
			// http://db.apache.org/derby/docs/10.4/devguide/rdevcsecure26537.html
		} catch (SQLException e) {
			// a normal shutdown of derby throws an SQL exception
			// with error code 50000 (for single database shutdown
			// 45000), otherwise an error occurred
			log.info("exception: {}", e.getErrorCode());
			if (e.getErrorCode() != 45000 && e.getErrorCode() != 50000)
				log.error(e.getMessage(), e);
			else {
				closed = true;
				log.info("database closed");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public Connection createConnection() {
		log.trace("create connection: {}", url);
		try {
			if (pool != null) {
				return pool.getConnection();
			} else {
				log.warn("no connection pool set up for {}", url);
				return DriverManager.getConnection(url);
			}
		} catch (Exception e) {
			log.error("Failed to create database connection", e);
			return null;
		}
	}

	public EntityManagerFactory getEntityFactory() {
		return entityFactory;
	}

	public int getVersion() {
		try {
			AtomicInteger aint = new AtomicInteger();
			String query = "select version from sophena_version";
			NativeSql.on(this).query(query, (result) -> {
				aint.set(result.getInt(1));
				return true;
			});
			return aint.get();
		} catch (Exception e) {
			log.error("failed to get the database version", e);
			return -1;
		}
	}

	public <T> boolean contains(Class<T> type, String id) {
		return get(type, id) != null;
	}

	public <T> T get(Class<T> type, String id) {
		if (id == null)
			return null;
		log.trace("get {} for id={}", type, id);
		try (var em = newEntityManager()) {
			return em.find(type, id);
		} catch (Exception e) {
			log.error("Error while loading {} with id {}", type, id, e);
			return null;
		}
	}

	public <T> List<T> getAll(Class<T> type, List<String> ids) {
		try (var em = newEntityManager()) {
			var jpql = "SELECT o FROM " + type.getSimpleName()
					+ " o WHERE o.id IN :ids";
			var query = em.createQuery(jpql, type);
			query.setParameter("ids", ids);
			return query.getResultList();
		} catch (Exception e) {
			log.error("Error while loading {} for ids", type, e);
			return Collections.emptyList();
		}
	}

	public <T> List<T> getAll(Class<T> type) {
		try (var em = newEntityManager()) {
			var jpql = "SELECT o FROM " + type.getSimpleName() + " o";
			var query = em.createQuery(jpql, type);
			return query.getResultList();
		} catch (Exception e) {
			log.error("Error while loading all instances of {}", type, e);
			return Collections.emptyList();
		}
	}

	public <T> T insert(T entity) {
		if (entity == null)
			return null;
		try (var em = newEntityManager()) {
			em.getTransaction().begin();
			em.persist(entity);
			em.getTransaction().commit();
			return entity;
		} catch (Exception e) {
			log.error("Error while inserting {}", entity, e);
			return entity;
		}
	}

	public <T> T update(T entity) {
		if (entity == null)
			return null;
		try (var em = newEntityManager()) {
			em.getTransaction().begin();
			T merged = em.merge(entity);
			em.getTransaction().commit();
			return merged;
		} catch (Exception e) {
			log.error("Error while updating {}", entity, e);
			return entity;
		}
	}

	public <T> void delete(T entity) {
		if (entity == null)
			return;
		try (var em = newEntityManager()) {
			em.getTransaction().begin();
			em.remove(em.merge(entity));
			em.getTransaction().commit();
		} catch (Exception e) {
			log.error("Error while deleting {}", entity, e);
		}
	}

	private EntityManager newEntityManager() {
		return entityFactory.createEntityManager();
	}

}
