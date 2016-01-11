package sophena.io;

import java.io.File;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sophena.model.LoadProfile;
import sophena.model.Stats;

public class LoadProfileIOTest {

	private LoadProfile profile;
	private File testFile;

	@Before
	public void setUp() throws Exception {
		profile = new LoadProfile();
		profile.dynamicData = new double[Stats.HOURS];
		profile.staticData = new double[Stats.HOURS];
		for (int i = 0; i < Stats.HOURS; i++) {
			profile.dynamicData[i] = Math.random() * 1000;
			profile.staticData[i] = Math.random() * 1000;
		}
		testFile = Files.createTempFile("_sophena_", ".csv").toFile();
	}

	@After
	public void tearDown() throws Exception {
		Files.delete(testFile.toPath());
	}

	@Test
	public void testIO() {
		LoadProfileWriter writer = new LoadProfileWriter();
		writer.write(profile, testFile);
		LoadProfileReader reader = new LoadProfileReader();
		LoadProfile copy = reader.read(testFile);
		for (int i = 0; i < Stats.HOURS; i++) {
			Assert.assertEquals(profile.dynamicData[i], copy.dynamicData[i], 1e-10);
			Assert.assertEquals(profile.staticData[i], copy.staticData[i], 1e-10);
		}
	}

}
