package sophena.model;

import org.junit.Assert;
import org.junit.Test;

public class CloneConsumerTest {

	@Test
	public void testCloneLoadProfiles() {
		Consumer origin = new Consumer();
		LoadProfile profile = new LoadProfile();
		origin.profile = profile;
		profile.staticData = new double[Stats.HOURS];
		profile.dynamicData = new double[Stats.HOURS];
		for (int i = 0; i < Stats.HOURS; i++) {
			profile.staticData[i] = Math.random();
			profile.dynamicData[i] = Math.random();
		}
		Consumer clone = origin.copy();
		Assert.assertTrue(clone.profile != null);
		LoadProfile clonedProfile = clone.profile;
		Assert.assertTrue(profile.staticData != clonedProfile.staticData);
		for (int i = 0; i < Stats.HOURS; i++) {
			Assert.assertEquals(profile.staticData[i],
					clonedProfile.staticData[i], 1e-16);
			Assert.assertEquals(profile.dynamicData[i],
					clonedProfile.dynamicData[i], 1e-16);
		}
	}

}
