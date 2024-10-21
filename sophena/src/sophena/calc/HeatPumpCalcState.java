package sophena.calc;

import java.util.ArrayList;
import java.util.List;

import sophena.model.Producer;
import sophena.model.Project;

public class HeatPumpCalcState {
	private SolarCalcLog log;
	private Project project;
	private Producer producer;
	
	public HeatPumpCalcState(SolarCalcLog log, Project project, Producer producer)
	{
		this.log = log;
		this.project = project;
		this.producer = producer;
	}
	
	public void calc(int hour, double TR, double TV)
	{
		if(producer.heatPump == null)
			return;
		
		// 1..365
		int JT = 1 + hour / 24;
		// 1..24
		int TS = 1 + hour % 24;
		
		List<Integer> list = new ArrayList<>();
		for(var i = 0; i < producer.heatPump.targetTemperature.length; i++)
		{
			
		}
	}
}
