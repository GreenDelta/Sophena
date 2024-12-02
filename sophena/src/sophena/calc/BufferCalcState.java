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
	private double QP_VT;

	// kWh
	private double QP_100;

	// 0..1
	private double FG;

	// °C
	private double TE;

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
	private double qLoadedVTInHour;

	// kWh
	private double qUnloadedHTInHour;

	// kWh
	private double qUnloadedNTInHour;

	// kWh
	private double qUnloadedVTInHour;

	// kWh
	private double qLostHTInHour;

	// kWh
	private double qLostNTInHour;

	// kWh
	private double qLostVTInHour;

	public BufferCalcState(Project project, SolarCalcLog log)
	{
		this.project = project;
		this.log = log;

		SeasonalItem seasonalItem = SeasonalItem.calc(project.heatNet, 0);
		TV = seasonalItem.flowTemperature;
		TR = seasonalItem.returnTemperature;

		TMAX = project.heatNet.maxBufferLoadTemperature;
		QP_HT = seasonalItem.targetChargeLevel * maxCapacity(project.heatNet);
		QP_NT = 0;
		QP_VT = 0;
		
		bufferLossFactor = lossFactor(project.heatNet);
	}

	public void preStep(int hour)
	{
		SeasonalItem seasonalItem = SeasonalItem.calc(project.heatNet, hour);

		this.maxTargetLoadFactor = seasonalItem.targetChargeLevel;

		TV = seasonalItem.flowTemperature;
		TR = seasonalItem.returnTemperature;

		QP_MAX = maxCapacity(project.heatNet);
		QP_100 = capacity100(project.heatNet);
		
		int jt = 1 + hour / 24;
		int ts = 1 + hour % 24;
		if(ts == 1)
		{
			log.beginProducer(null);
			log.beginDay(jt, ts, "QP_MAX [kWh]", "QP_100 [kWh]", "QP_HT [kWh]", "QP_VT [kWh]", "QP_NT [kWh]", "QP100_NT [kWH]", "TV [°C]", "TR [°C]", "FG", "TE [°C]", "TMAX [°C]",
					"qLoadedHT [kWh]",
					"qLoadedNT [kWh]",
					"qLoadedVT [kWh]",
					"qUnloadedHT [kWh]",
					"qUnloadedNT [kWh]",
					"qUnloadedVT [kWh]",
					"qLostHT [kWh]",
					"qLostNT [kWh]",
					"qLostVT [kWh]"
			);
		}
		
		qLoadedHTInHour = 0;
		qLoadedNTInHour = 0;
		qLoadedVTInHour = 0;
		qUnloadedHTInHour = 0;
		qUnloadedNTInHour = 0;
		qUnloadedVTInHour = 0;
		qLostHTInHour = 0;
		qLostNTInHour = 0;
		qLostVTInHour = 0;
	}

	public double load(int hour, double qToLoad, BufferCalcLoadType loadType, boolean useMaxTargetLoadFactor)
	{
		// Prevent QP_NT from becoming more and more negative due to loss and only loading high temperature.
		
		if(loadType != BufferCalcLoadType.NT && QP_NT < 0)
		{
			loadType = BufferCalcLoadType.NT;
		}
		else if(loadType == BufferCalcLoadType.HT && QP_VT < 0)
		{
			loadType = BufferCalcLoadType.VT;
		}
		
		double Qloaded;
		if(loadType == BufferCalcLoadType.HT)
		{
			Qloaded = Math.min(qToLoad, CalcHTCapacity(useMaxTargetLoadFactor));
			QP_HT = QP_HT + Qloaded;

			qLoadedHTInHour += Qloaded;
		}
		else if(loadType == BufferCalcLoadType.VT)
		{
			Qloaded = Math.min(qToLoad, CalcNTCapacity(useMaxTargetLoadFactor));
			QP_VT = QP_VT + Qloaded;

			qLoadedVTInHour += Qloaded;
		}			
		else
		{
			Qloaded = Math.min(qToLoad,CalcNTCapacity(useMaxTargetLoadFactor));
			QP_NT = QP_NT + Qloaded;

			qLoadedNTInHour += Qloaded;
		}

		double qToLoadRemaining = qToLoad - Qloaded; 

		UpdateFGAndTE();

		return qToLoadRemaining;
	}
	
	public double unload(int hour, double qToUnload, BufferCalcLoadType loadType)
	{
		double Qunloaded;
		if(loadType == BufferCalcLoadType.HT)
		{
			Qunloaded = Math.max(0, Math.min(qToUnload, QP_HT));
			Qunloaded = Math.min(Qunloaded, project.heatNet.maximumPerformance);
			QP_HT = QP_HT - Qunloaded;

			qUnloadedHTInHour += Qunloaded;
		}
		else if(loadType == BufferCalcLoadType.VT)
		{
			Qunloaded = Math.max(0, Math.min(qToUnload, QP_VT));
			Qunloaded = Math.min(Qunloaded, project.heatNet.maximumPerformance);
			QP_VT = QP_VT - Qunloaded;

			qUnloadedVTInHour += Qunloaded;
		}			
		else
		{
			Qunloaded = Math.max(0, Math.min(qToUnload, QP_NT));
			Qunloaded = Math.min(Qunloaded, project.heatNet.maximumPerformance);
			QP_NT = QP_NT - Qunloaded;

			qUnloadedNTInHour += Qunloaded;
		}
		
		double qToUnloadRemaining = qToUnload - Qunloaded;


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
			Qlost = Math.min(qLossRemaining, QP_VT);
			QP_VT -= Qlost;

			qLostVTInHour += Qlost;
			qLossRemaining -= Qlost;
		}

		if(qLossRemaining > 0)
		{
			Qlost = qLossRemaining;
			QP_NT -= Qlost;

			qLostNTInHour += Qlost;
		}
		
		//UpdateFGAndTE();

		return Qlost;
	}
	
	public void postStep(int hour) 
	{
		log.beginProducer(null);
		log.hourValues(hour, true, QP_MAX, QP_100, QP_HT, QP_VT, QP_NT, QP100_NT(), TV, TR, FG, TE, TMAX,
				qLoadedHTInHour,
				qLoadedNTInHour,
				qLoadedVTInHour,
				qUnloadedHTInHour,
				qUnloadedNTInHour,
				qUnloadedVTInHour,
				qLostHTInHour,
				qLostNTInHour,
				qLostVTInHour
		);
	}
	
	public double totalUnloadableHTPower()
	{
		return Math.min(QP_HT, project.heatNet.maximumPerformance);
	}
	
	public double totalUnloadableVTPower()
	{
		return Math.min(QP_VT, project.heatNet.maximumPerformance);
	}
	
	public double getLoadFactor(boolean useMaxTargetLoadFactor)
	{
		double loadFactor = useMaxTargetLoadFactor ? maxTargetLoadFactor : 1.0;
		
		double denominator = QP_MAX * loadFactor;
		if(denominator == 0)
			return 0;
		
		return (QP_HT + QP_VT + QP_NT) / denominator;
	}

	public double CalcHTCapacity(boolean useMaxTargetLoadFactor)
	{
		double loadFactor = useMaxTargetLoadFactor ? maxTargetLoadFactor : 1.0;

		return Math.max(0, QP_MAX * loadFactor - QP_HT - QP_NT - QP_VT);
	}
	
	public double CalcNTCapacity(boolean useMaxTargetLoadFactor)
	{
		double loadFactor = useMaxTargetLoadFactor ? maxTargetLoadFactor : 1.0;

		return Math.max(0, Math.min(QP_MAX * loadFactor - QP_HT, QP100_NT()) - QP_NT - QP_VT);
	}

	private void UpdateFGAndTE()
	{
		if (project.heatNet != null && project.heatNet.bufferTank == null)
		{
			TE = TV;
			return;
		}
		
		FG = (QP_VT + QP_NT) / QP100_NT(); 
		
		if(FG < 0.8)
		{
			TE = TR + 5.0 / 12.0 * (TV - TR) * FG;
		}
		else if(FG < 1.0)
		{
			TE = TR + 1.0 / 3.0 * (TV - TR) * (10.0 * FG - 7.0);
		}
		else 
		{			
			TE = TMAX;
		}
		
		TE = Math.max(TE,TR);
	}
	
	public double averageTemperature()
	{
		if(QP_MAX == 0)
			return TR;
		return TR + (TMAX - TR) * (QP_HT + QP_NT + QP_VT) / QP_MAX;
	}

	public double QP100_NT()
	{
		if(QP_100 == 0)
			return 0;
		return (1 - QP_HT / QP_MAX) * QP_100;
	}


	/**
	 * Calculates the buffer tank capacity of the given heating net
	 * specification, in kWh.
	 */
	private double maxCapacity(HeatNet net) {
		return calcCapacity(net, TR, TMAX);
	}
	
	private double capacity100(HeatNet net) {
		return calcCapacity(net, TR, TV);
	}

	private double calcCapacity(HeatNet net, double minTemp, double maxTemp) {

		if (net == null || net.bufferTank == null)
			return 0;
		double volume = net.bufferTank.volume; // liters
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
		double r = (buffer.diameter / 1000.0 - (2.0 * ins)) / 2.0;
		double h = buffer.height / 1000.0 - (2.0 * ins);
		double area = 2.0 * Math.PI * r * (r + h);
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
		double deltaTemp = (minTemp + fillRate * (maxTemp - minTemp)) - 20.0;
		return lossFactor * deltaTemp / 1000.0;
	}

	/**
	 * Returns the buffer loss in kWh for the given fill rate (a value between 0 and 1).
	 */
	private double calcLoss(HeatNet net, double lossFactor) {
		if (net == null || net.bufferTank == null)
			return 0;
		double avgBufferTemp = averageTemperature();
		double averageRoomTemp = 20.0;

		return lossFactor * Math.max(0, avgBufferTemp - averageRoomTemp) / 1000.0;
	}
	
	public double getTE() { return TE; }
	public double getTV() { return TV; }
	public double getTR() { return TR; }
	public double getTMAX() { return TMAX; }
}
