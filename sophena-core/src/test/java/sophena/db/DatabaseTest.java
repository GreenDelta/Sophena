package sophena.db;

import java.sql.Connection;

import org.junit.Assert;
import org.junit.Test;

import sophena.Tests;

public class DatabaseTest {

	@Test
	public void testGetConnection() throws Exception {
		Database db = Tests.getDb();
		Connection con = db.createConnection();
		con.close();
	}

	@Test
	public void testGetVersion() throws Exception {
		Database db = Tests.getDb();
		int version = db.getVersion();
		Assert.assertTrue(version > 0);
	}

}
