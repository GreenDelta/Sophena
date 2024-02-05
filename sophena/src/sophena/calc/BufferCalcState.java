package sophena.calc;

import sophena.math.energetic.SeasonalItem;
import sophena.model.BufferTank;
import sophena.model.HeatNet;
import sophena.model.Project;

public class BufferCalcState {
	private Project project;
	private SolarCalcLog log;

	// kWh
	private double QP_HT; 

	// kWh
	private double QP_NT;
	
	// kWh
	private double QP_100;

	// 0..1
	private double FG;

	// °C
	public double TE;

	// °C
	private double TR;
	
	// °C
	private double TV;
	
	// °C
	private double TMAX;
		
	// kWH
	private double QP_MAX;
	
	private double maxTargetLoadFactor;
	
	private double bufferLossFactor;

	// kWh
	private double qLoadedHTInHour;

	// kWh
	private double qLoadedNTInHour;

	// kWh
	private double qUnloadedHTInHour;

	// kWh
	private double qUnloadedNTInHour;

	// kWh
	private double qLostHTInHour;

	// kWh
	private double qLostNTInHour;

	public BufferCalcState(Project project, SolarCalcLog log)
	{
		this.project = project;
		this.log = log;
		
		QP_HT = 0.5 * maxCapacity(project.heatNet);
		QP_NT = 0;
		TMAX = project.heatNet.maxBufferLoadTemperature;
		
		bufferLossFactor = lossFactor(project.heatNet);
	}

	public void preStep(int hour)
	{
		SeasonalItem seasonalItem = SeasonalItem.calc(project.heatNet, hour);

		this.maxTargetLoadFactor = seasonalItem.targetChargeLevel;

		QP_MAX = maxCapacity(project.heatNet);
		QP_100 = capacity100(project.heatNet);
		TV = seasonalItem.flowTemperature;
		TR = seasonalItem.returnTemperature;
		
		int jt = 1 + hour / 24;
		int ts = 1 + hour % 24;
		if(ts == 1)
		{
			log.beginProducer(null);
			log.beginDay(jt, ts, "QP_MAX [kWh]", "QP_100 [kWh]", "QP_HT [kWh]", "QP_NT [kWh]", "TV [°C]", "TR [°C]", "FG", "TE [°C]", "TMAX [°C]",
					"qLoadedHT [kWh]",
					"qLoadedNT [kWh]",
					"qUnloaded [kWh]",
					"qUnloaded [kWh]",
					"qLostHT [kWh]",
					"qLostNT [kWh]"
			);
		}
		
		qLoadedHTInHour = 0;
		qLoadedNTInHour = 0;
		qUnloadedHTInHour = 0;
		qUnloadedNTInHour = 0;
		qLostHTInHour = 0;
		qLostNTInHour = 0;
	}

	public double load(int hour, double qToLoad, boolean isHT)
	{
		// Prevent QP_NT from becoming more and more negative due to loss and only loading high temperature.
		
		if(isHT && QP_NT < 0)
		{
			isHT = false;
		}
		
		double Qloaded;
		if(isHT)
		{
			Qloaded = Math.min(qToLoad, CalcHTCapacity());
			QP_HT = QP_HT + Qloaded;

			qLoadedHTInHour += Qloaded;
}
		else
		{
			Qloaded = Math.min(qToLoad,CalcNTCapacity());
			QP_NT = QP_NT + Qloaded;

			qLoadedNTInHour += Qloaded;
}

		double qToLoadRemaining = qToLoad - Qloaded; 

		//log.beginProducer(null);
		//log.message(String.format("BufferCalcState.load(%s, %f, %s) -> loaded %f, still to load %f", hour, qToLoad, isHT ? "HT" : "NT", Qloaded, qToLoadRemaining));

		UpdateFGAndTE();

		return qToLoadRemaining;
	}
	
	public double unload(int hour, double qToUnload, boolean isHT)
	{
		double Qunloaded;
		if(isHT)
		{
			Qunloaded = Math.max(0, Math.min(qToUnload, QP_HT));
			Qunloaded = Math.min(Qunloaded, project.heatNet.maximumPerformance);
			QP_HT = QP_HT - Qunloaded;

			qUnloadedHTInHour += Qunloaded;
}
		else
		{
			Qunloaded = Math.max(0, Math.min(qToUnload, QP_NT));
			Qunloaded = Math.min(Qunloaded, project.heatNet.maximumPerformance);
			QP_NT = QP_NT - Qunloaded;

			qUnloadedNTInHour += Qunloaded;
		}
		
		double qToUnloadRemaining = qToUnload - Qunloaded;

		//log.beginProducer(null);
		//log.message(String.format("BufferCalcState.unload(%s, %f, %s) -> unloaded %f, still to unload %f", hour, qToUnload, isHT ? "HT" : "NT", Qunloaded, qToUnloadRemaining));

		UpdateFGAndTE();

		return qToUnloadRemaining;
	}
	
	public double applyLoss(int hour)
	{
		double qLoss = calcLoss(project.heatNet, bufferLossFactor);
		
		double Qlost = Math.min(qLoss, QP_HT);
		QP_HT = QP_HT - Qlost;

		qLostHTInHour += Qlost;

		double qLossRemaining = qLoss - Qlost;
		
		if(qLossRemaining > 0)
		{
			Qlost = qLossRemaining;
			QP_NT = QP_NT - Qlost;

			qLostNTInHour += Qlost;
		}
		
		//log.beginProducer(null);
		//log.message(String.format("BufferCalcState.applyLoss(%s, %f) -> lost %f", hour, qLoss, Qlost));

		UpdateFGAndTE();

		return Qlost;
	}
	
	public void postStep(int hour) 
	{
		log.beginProducer(null);
		log.hourValues(hour, QP_MAX, QP_100, QP_HT, QP_NT, TV, TR, FG, TE, TMAX,
				qLoadedHTInHour,
				qLoadedNTInHour,
				qUnloadedHTInHour,
				qUnloadedNTInHour,
				qLostHTInHour,
				qLostNTInHour
		);
	}
	
	public double totalUnloadablePower()
	{
		return Math.min(QP_HT + QP_NT, project.heatNet.maximumPerformance);
	}
	
	public double getLoadFactor()
	{
		return (QP_HT + QP_NT) / (QP_MAX * maxTargetLoadFactor);
	}

	public double CalcHTCapacity()
	{
		return Math.max(0, QP_MAX * maxTargetLoadFactor - QP_HT - QP_NT);
	}
	
	public double CalcNTCapacity()
	{
		return Math.max(0, Math.min(QP_MAX * maxTargetLoadFactor, QP_100 - QP_HT));
	}

	private void UpdateFGAndTE()
	{
		FG = QP_NT / QP_100; 
		
		if(FG < 0.8)
		{
			TE = TR + 5/12 * (TV - TR) * FG;
		}
		else if(FG < 1.0)
		{
			TE = TR + 1/3 * (TV - TR) * (10 * FG - 7);
		}
		else 
		{
			TE = TMAX;
		}
		
		TE = Math.max(TE,TR);
	}
	
	public double averageTemperature()
	{
		return TR + (TMAX - TR) * (QP_HT + QP_NT) / QP_MAX;
	}

	/**
	 * Calculates the buffer tank capacity of the given heating net
	 * specification, in kWh.
	 */
	private double maxCapacity(HeatNet net) {
		if (net == null || net.bufferTank == null)
			return 0;
		double volume = net.bufferTank.volume; // liters
		double maxTemp = net.maxBufferLoadTemperature;
		double minTemp = net.lowerBufferLoadTemperature != null
				? net.lowerBufferLoadTemperature
				: net.returnTemperature;
		return 0.001166 * volume * (maxTemp - minTemp);
	}

	private double capacity100(HeatNet net) {
		if (net == null || net.bufferTank == null)
			return 0;
		double volume = net.bufferTank.volume; // liters
		double maxTemp = net.supplyTemperature;
		double minTemp = net.lowerBufferLoadTemperature != null
				? net.lowerBufferLoadTemperature
				: net.returnTemperature;
		return 0.001166 * volume * (maxTemp - minTemp);
	}

	/**
	 * Calculates the static loss factor of the buffer tank in the given
	 * heatnet. This value is then combined with the current buffer state in an
	 * simulation step to calculate the buffer loss (see the loss function).
	 */
	public static double lossFactor(HeatNet net) {
		if (net == null || net.bufferTank == null)
			return 0;
		BufferTank buffer = net.bufferTank;
		double ins = buffer.insulationThickness / 1000;
		if (ins < 0.001) {
			ins = 0.001;
		}
		double r = (buffer.diameter / 1000 - (2 * ins)) / 2;
		double h = buffer.height / 1000 - (2 * ins);
		double area = 2 * Math.PI * r * (r + h);
		double uValue = net.bufferLambda / ins;
		return area * uValue;
	}

	/**
	 * Returns the buffer loss for the given fill rate (a value between 0 and
	 * 1).
	 */
	@Deprecated
	public static double loss(HeatNet net, double lossFactor, double fillRate) {
		if (net == null || lossFactor == 0)
			return 0;
		double maxTemp = net.maxBufferLoadTemperature;
		double minTemp = net.lowerBufferLoadTemperature != null
				? net.lowerBufferLoadTemperature
				: net.supplyTemperature;
		double deltaTemp = (minTemp + fillRate * (maxTemp - minTemp)) - 20;
		return lossFactor * deltaTemp / 1000;
	}

	/**
	 * Returns the buffer loss in kWh for the given fill rate (a value between 0 and 1).
	 */
	private double calcLoss(HeatNet net, double lossFactor) {
		if (net == null || lossFactor == 0)
			return 0;
		
		double avgBufferTemp = averageTemperature();
		double averageRoomTemp = 20;

		return lossFactor * Math.max(0, avgBufferTemp - averageRoomTemp) / 1000;
	}
}
