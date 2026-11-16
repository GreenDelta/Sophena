package sophena.calc.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import sophena.model.Project;

public class EnergyResultTest {

	@Test
	public void testProjectWithoutHeatNet() {
		var project = new Project();
		var e = assertThrows(IllegalArgumentException.class,
			() -> EnergyResult.calculate(project));
		assertEquals("Das Projekt hat kein Wärmenetz.", e.getMessage());
	}
}
