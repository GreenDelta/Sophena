package sophena.model;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Test;

public class CostSettingsCloneTest {

	@Test
	public void testCloneDoubleFields() throws Exception {
		CostSettings cs = new CostSettings();
		for (Field field : CostSettings.class.getDeclaredFields()) {
			if (!field.getType().equals(double.class))
				continue;
			field.setDouble(cs, 42.0);
		}
		cs = cs.copy();
		for (Field field : CostSettings.class.getDeclaredFields()) {
			if (!field.getType().equals(double.class))
				continue;
			double val = field.getDouble(cs);
			Assert.assertEquals(
					"Failed to clone field " + field, 42.0, val, 1e-6);
		}
	}
}
