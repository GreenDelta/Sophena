package sophena.calc.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import sophena.calc.ProjectResult;
import sophena.model.Project;

public class EnergyResultTest {

	@Test
	public void testProjectWithoutHeatNet() {
		var project = new Project();
		var res = EnergyResult.calculate(project);
		assertTrue(res.isError());
		assertEquals("Das Projekt hat kein Wärmenetz.", res.error());

		// the error is passed to the project result
		var projectRes = ProjectResult.calculate(project);
		assertTrue(projectRes.isError());
		assertEquals("Das Projekt hat kein Wärmenetz.", projectRes.error());
	}
}
