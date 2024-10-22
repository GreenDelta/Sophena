package sophena.calc;

import java.util.ArrayList;
import java.util.List;
import sophena.model.Producer;
import sophena.model.Project;

public class HeatPumpCalcState {
	private SolarCalcLog log;
	private Project project;
	private Producer producer;
	private BufferCalcLoadType bufferLoadType;

	private double TQ;
	private double maxPower;
	private double cop;
	
	private double Q_used;
	private double Q_el_used;

	public HeatPumpCalcState(SolarCalcLog log, Project project, Producer producer)
	{
		this.log = log;
		this.project = project;
		this.producer = producer;
	}
	
	public void calcPre(int hour, double TR, double TV)
	{
		bufferLoadType = BufferCalcLoadType.None;

		consumedPower = 0;
		maxPower = 0;
		cop = 0;

		if(producer.heatPump == null)
			return;
		
		if(hour == 0)
		{
			Q_used = 0;
			Q_el_used = 0;
		}

		TQ = calcTQ(hour);
		if(Double.isNaN(TQ))
			return;
		
		// Find largest temperature below and smallest above TV. Same for TR.
		
		double maxTemperatureSmallerThanTV = Double.MIN_VALUE;
		double maxTemperatureSmallerThanTR = Double.MIN_VALUE;
		double minTemperatureGreaterThanTV = Double.MAX_VALUE;
		double minTemperatureGreaterThanTR = Double.MAX_VALUE;
		
		for(var i = 0; i < producer.heatPump.targetTemperature.length; i++)
		{
			double temperature = producer.heatPump.targetTemperature[i];
			
			if(temperature < TV)
				maxTemperatureSmallerThanTV = Math.max(maxTemperatureSmallerThanTV, temperature);
			if(temperature < TR)
				maxTemperatureSmallerThanTR = Math.max(maxTemperatureSmallerThanTR, temperature);
			
			if(temperature >= TV)
				minTemperatureGreaterThanTV = Math.min(minTemperatureGreaterThanTV, temperature);
			if(temperature >= TR)
				minTemperatureGreaterThanTR = Math.min(minTemperatureGreaterThanTR, temperature);
		}
		
		// Determine which curves to use as possible upper curves for TV and TR

		List<Integer> upperIndicesForTV = new ArrayList<>();
		List<Integer> upperIndicesForTR = new ArrayList<>();

		for(var i = 0; i < producer.heatPump.targetTemperature.length; i++)
		{
			double temperature = producer.heatPump.targetTemperature[i];
			if(temperature == minTemperatureGreaterThanTV)
				upperIndicesForTV.add(i);
			else if(temperature == minTemperatureGreaterThanTR)
				upperIndicesForTR.add(i);
		}
		
		// At least one curve must have at least two points

		if(upperIndicesForTV.size() < 2 && upperIndicesForTR.size() < 2)
		{
			log.message("Exit because at least one curve must have at least two points");
			return;
		}
		
		// Decide which upper curve to use

		boolean useTV = upperIndicesForTV.size() > 1;
		List<Integer> upperIndices = useTV ? upperIndicesForTV : upperIndicesForTR;
		double maxTemperatureSmaller = useTV ? maxTemperatureSmallerThanTV : maxTemperatureSmallerThanTR;
		
		// Determine which curve to use as lower curve 

		List<Integer> lowerIndices = new ArrayList<>();
		for(var i = 0; i < producer.heatPump.targetTemperature.length; i++)
		{
			double temperature = producer.heatPump.targetTemperature[i];

			if(temperature == maxTemperatureSmaller)
				lowerIndices.add(i);
		}
		
		// Determine left and right point indices within the upper curve based on source temperature

		int indexLeftUpper = -1;
		int indexRightUpper = -1;

		int indexLeftLower = -1;
		int indexRightLower = -1;

		for(var i = 0; i < upperIndices.size(); i++)
		{
			var upperIndex = upperIndices.get(i);
			var temperature = producer.heatPump.sourceTemperature[upperIndex];
			
			if(temperature <= TQ)
			{
				if(indexLeftUpper == -1)
					indexLeftUpper = upperIndex;
				else if(temperature > producer.heatPump.sourceTemperature[indexLeftUpper])
					indexLeftUpper = upperIndex;
			}

			if(temperature > TQ)
			{
				if(indexRightUpper == -1)
					indexRightUpper = upperIndex;
				else if(temperature < producer.heatPump.sourceTemperature[indexRightUpper])
					indexRightUpper = upperIndex;
			}
		}

		// Exit if source temperature is outside upper curve
		
		if(indexLeftUpper == -1 || indexRightUpper == -1)
		{
			log.message("Exit because source temperature is outside upper curve");
			return;
		}
		
		// Determine left and right point indices within the lower curve based on source temperature
		
		for(var i = 0; i < lowerIndices.size(); i++)
		{
			var lowerIndex = lowerIndices.get(i);
			var temperature = producer.heatPump.sourceTemperature[lowerIndex];
			
			if(temperature <= TQ)
			{
				if(indexLeftLower == -1)
					indexLeftLower = lowerIndex;
				else if(temperature > producer.heatPump.sourceTemperature[indexLeftLower])
					indexLeftLower = lowerIndex;
			}

			if(temperature > TQ)
			{
				if(indexRightLower == -1)
					indexRightLower = lowerIndex;
				else if(temperature < producer.heatPump.sourceTemperature[indexRightLower])
					indexRightLower = lowerIndex;
			}
		}
		
		// Either interpolate between upper and lower curve, or just use the upper curve
		
		// Interpolate max power and COP for upper curve, by TQ
		double upperK = findLerpK(producer.heatPump.sourceTemperature[indexLeftUpper], producer.heatPump.sourceTemperature[indexRightUpper], TQ);
		double upperMaxPower = lerp(producer.heatPump.maxPower[indexLeftUpper], producer.heatPump.maxPower[indexRightUpper], upperK);
		double upperCOP = lerp(producer.heatPump.cop[indexLeftUpper], producer.heatPump.cop[indexRightUpper], upperK);

		if(indexLeftLower == -1 || indexRightLower == -1)
		{						
			maxPower = upperMaxPower;
			cop = upperCOP;
		}
		else
		{
			// Interpolate max power and COP for lower curve, by TQ

			double lowerK = findLerpK(producer.heatPump.sourceTemperature[indexLeftLower], producer.heatPump.sourceTemperature[indexRightLower], TQ);
			double lowerMaxPower = lerp(producer.heatPump.maxPower[indexLeftLower], producer.heatPump.maxPower[indexRightLower], lowerK);
			double lowerCOP = lerp(producer.heatPump.cop[indexLeftLower], producer.heatPump.cop[indexRightLower], lowerK);

			// Interpolate between interpolated values of upper and lower curve, by  targetTemperature

			double targetTemperature = useTV ? TV : TR;
			double k = findLerpK(producer.heatPump.targetTemperature[indexLeftLower], producer.heatPump.targetTemperature[indexLeftUpper], targetTemperature);
			maxPower = lerp(lowerMaxPower, upperMaxPower, k);
			cop = lerp(lowerCOP, upperCOP, k);
		}

		// The buffer load type depends on which upper curve was chosen

		bufferLoadType = useTV ? BufferCalcLoadType.VT : BufferCalcLoadType.NT;
	}
	
	public void calcPost(int hour)
	{
		if(bufferLoadType != BufferCalcLoadType.None)
		{
			Q_used += consumedPower;
			Q_el_used += consumedPower / cop;
		}
		
		log.beginProducer(producer);

		int JT = 1 + hour / 24;
		int TS = 1 + hour % 24;
		
		if(TS == 1){
			log.beginDay(
				JT,
				hour,
				"Hour",
				"TQ",
				"maxPower",
				"COP",
				"Load type"
			);
		}

		log.hourValues(hour,
			true,
			TS,
			TQ,
			maxPower,
			cop,
			bufferLoadType
		);
	}
	
	private double calcTQ(int hour)
	{
		switch(producer.heatPumpMode)
		{
		case OUTODOOR_TEMPERATURE_MODE:
			return project.weatherStation.data[hour];
		case USER_TEMPERATURE_MODE:
			return producer.sourceTemperatureUser;
		case HOURLY_TEMPERATURE_MODE:
			return producer.sourceTemperatureHourly[hour];
		default:
			return Double.NaN;
		}
	}
	
	private static double findLerpK(double left, double right, double value)
	{
		return (value - left) / (right - left);
	}
	
	private static double lerp(double a,double b, double t)
	{
		return a * (1 - t) + b * t;
	}
	
	public BufferCalcLoadType getBufferLoadType() {
		
		return bufferLoadType; 
	}
	
	public double getMaxPower()
	{
		return maxPower;
	}

	public double getCOP()
	{
		return cop;
	}

	private double consumedPower;

	public void setConsumedPower(double consumedPower)
	{
		this.consumedPower = producer.utilisationRate == null || producer.utilisationRate == 0
			? 0
			: consumedPower / producer.utilisationRate;
	}

	public double getJAZ()
	{
		return Q_el_used > 0 ? Q_used / Q_el_used : 0;
	}
}
