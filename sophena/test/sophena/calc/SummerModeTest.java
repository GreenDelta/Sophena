package sophena.calc;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import sophena.model.Boiler;
import sophena.model.BufferTank;
import sophena.model.Consumer;
import sophena.model.CostSettings;
import sophena.model.HeatNet;
import sophena.model.LoadProfile;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.Project;
import sophena.model.Stats;

public class SummerModeTest {

	@Test
	public void testSummerMode() {

		Project project = new Project();
		project.costSettings = new CostSettings();
		HeatNet net = new HeatNet();
		project.heatNet = net;
		net.supplyTemperature = 80;
		net.returnTemperature = 50;
		net.maxBufferLoadTemperature = 90.0;
		net.lowerBufferLoadTemperature = 75.0;

		Consumer cons = new Consumer();
		project.consumers.add(cons);
		cons.id = "cons";
		cons.profile = new LoadProfile();
		cons.profile.dynamicData = new double[Stats.HOURS];
		cons.profile.staticData = new double[Stats.HOURS];
		for (int i = 0; i < Stats.HOURS; i++) {
			cons.profile.dynamicData[i] = 25;
		}

		Producer prod = new Producer();
		project.producers.add(prod);
		prod.id = "prod";
		prod.name = "prod";
		prod.rank = 1;
		prod.function = ProducerFunction.BASE_LOAD;
		Boiler boiler = new Boiler();
		prod.boiler = boiler;
		boiler.minPower = 50;
		boiler.maxPower = 500;

		BufferTank buff = new BufferTank();
		buff.volume = 10000;
		net.bufferTank = buff;

		ProjectResult r = ProjectResult.calculate(project);
		assertTrue((r.energyResult.totalProducedHeat > (Stats.HOURS * 25.0)));
		// ExcelExport exp = new ExcelExport(project, r,
		// new File("target/summer_mode.xlsx"));
		// exp.run();
	}

}
