package sophena;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sophena.rcp.AppConfig;

public class AppConfigTest {

	@Before
	public void setUp() {
		AppConfig config = AppConfig.load();
		config.dataDir = System.getProperty("user.dir") + "/data";
		config.save();
	}

	@Test
	public void testAppDir() {
		AppConfig config = AppConfig.load();
		Assert.assertEquals(System.getProperty("user.dir") + "/data",
				config.dataDir);
	}

}
