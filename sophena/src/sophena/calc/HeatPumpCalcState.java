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
	private double TK_i;
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
		
		List<Integer> lowerIndices = new ArrayList<>();
		int indexLeftUpper = -1;
		int indexRightUpper = -1;

		int indexLeftLower = -1;
		int indexRightLower = -1;
		boolean useTV = true;
		// Find largest temperature below and smallest above TV. Same for TR.
		double destTemperature = TV;
		
		while(destTemperature > TR)
		{
			double maxTemperatureSmallerThanTV = Double.MIN_VALUE;
			double minTemperatureGreaterThanTV = Double.MAX_VALUE;
			double maxTemperatureBetweenTRAndTV = Double.MIN_VALUE;
			
			for(var i = 0; i < producer.heatPump.targetTemperature.length; i++)
			{
				double temperature = producer.heatPump.targetTemperature[i];
				
				if(temperature < destTemperature && useTV)
					maxTemperatureSmallerThanTV = Math.max(maxTemperatureSmallerThanTV, temperature);
				
				if(temperature >= destTemperature && useTV)
					minTemperatureGreaterThanTV = Math.min(minTemperatureGreaterThanTV, temperature);
				
				if(temperature > TR && temperature < destTemperature)
					maxTemperatureBetweenTRAndTV = Math.max(maxTemperatureBetweenTRAndTV, temperature);
			}
			
			// Determine which curves to use as possible upper curves for TV and TR

			List<Integer> upperIndicesForTV = new ArrayList<>();
			List<Integer> upperIndicesForTR = new ArrayList<>();

			for(var i = 0; i < producer.heatPump.targetTemperature.length; i++)
			{
				double temperature = producer.heatPump.targetTemperature[i];
				if(temperature == minTemperatureGreaterThanTV)
					upperIndicesForTV.add(i);
				else if(temperature == maxTemperatureBetweenTRAndTV)
					upperIndicesForTR.add(i);
			}
			
		
			// Decide which upper curve to use
			useTV = upperIndicesForTV.size() > 1;
			List<Integer> upperIndices = useTV ? upperIndicesForTV : upperIndicesForTR;
			double maxTemperatureSmaller = useTV ? maxTemperatureSmallerThanTV : Double.MIN_VALUE;
			
			// Upper curve must have at least two points
			if(upperIndices.size() < 2)
			{
				log.beginProducer(producer);
				log.message("Exit because each curve must have at least two points");
				return;
			}

			// Determine which curve to use as lower curve 

			lowerIndices.clear();
			for(var i = 0; i < producer.heatPump.targetTemperature.length; i++)
			{
				double temperature = producer.heatPump.targetTemperature[i];

				if(temperature == maxTemperatureSmaller)
					lowerIndices.add(i);
			}
			
			// Determine left and right point indices within the upper curve based on source temperature

			indexLeftUpper = -1;
			indexRightUpper = -1;

			indexLeftLower = -1;
			indexRightLower = -1;

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
				destTemperature = producer.heatPump.targetTemperature[upperIndices.get(0)] - 1;
				useTV = destTemperature > TV;
				log.beginProducer(producer);
				log.message("ReRun curver search because source temperature is outside upper curve");
			}
			else
				break;
		}
		
		if(destTemperature <= TR)
		{
			log.beginProducer(producer);
			log.message("No upper curve for source temperature found");
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
			TK_i = producer.heatPump.targetTemperature[indexLeftUpper];
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
			TK_i = targetTemperature;
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
				"TQ [°C]",
				"maxPower[kW]",
				"COP",
				"Load type",
				"consumedPower[W]"
			);
		}

		log.hourValues(hour,
			true,
			TS,
			TQ,
			maxPower,
			cop,
			bufferLoadType,
			consumedPower
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

	public double getTK_i()
	{
		return TK_i;
	}
	
	private double consumedPower;

	public void setConsumedPower(double consumedPower)
	{
		this.consumedPower = consumedPower;
	}

	public double getJAZ()
	{
		return Q_el_used > 0 ? Q_used / Q_el_used : 0;
	}
}
