package sophena.calc;

import sophena.math.energetic.Buffers;
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
	
	// %
	//private double HTV;

	// %
	//private double NTV;

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
	
	public BufferCalcState(Project project, SolarCalcLog log)
	{
		this.project = project;
		this.log = log;
		
		QP_HT = 0.5 * Buffers.maxCapacity(project.heatNet);
		QP_NT = 0;
		TMAX = project.heatNet.maxBufferLoadTemperature;
	}

	public void preStep(int hour, double maxTargetLoadFactor)
	{
		this.maxTargetLoadFactor = maxTargetLoadFactor;

		QP_MAX = Buffers.maxCapacity(project.heatNet);
		QP_100 = Buffers.capacity100(project.heatNet);
		TV = project.heatNet.supplyTemperature;
		TR = project.heatNet.returnTemperature;
		
		int jt = 1 + hour / 24;
		int ts = 1 + hour % 24;
		if(ts == 1)
		{
			log.beginProducer(null);
			log.beginDay(jt, ts, "QP_MAX [kWh]", "QP_100 [kWh]", "QP_HT [kWh]", "QP_NT [kWh]", "TV [°C]", "TR [°C]", "FG", "TE [°C]");
		}
	}

	private double calcMaxLoadHT()
	{
		return Math.max(0, QP_MAX * maxTargetLoadFactor - QP_NT);
	}

	private double calcMaxLoadNT()
	{
		return Math.max(0, QP_MAX * maxTargetLoadFactor - QP_HT);
	}

	public double load(int hour, double qToLoad, boolean isHT)
	{
		double Qloaded;
		if(isHT)
		{
			Qloaded = Math.min(qToLoad,CalcHTCapacity());
			Qloaded = Math.min(Qloaded, calcMaxLoadHT());
			QP_HT = QP_HT + Qloaded;
		}
		else
		{
			Qloaded = Math.min(qToLoad,CalcNTCapacity());
			Qloaded = Math.min(Qloaded, calcMaxLoadNT());
			QP_NT = QP_NT + Qloaded;
		}

		double qToLoadRemaining = qToLoad - Qloaded; 

		log.beginProducer(null);
		log.message(String.format("BufferCalcState.load(%s, %f, %s) -> loaded %f, still to load %f", hour, qToLoad, isHT ? "HT" : "NT", Qloaded, qToLoadRemaining));

		UpdateFGAndTE();
		
		return qToLoadRemaining;
	}
	
	public double unload(int hour, double qToUnload, boolean isHT)
	{
		double Qunloaded;
		if(isHT)
		{
			Qunloaded = Math.min(qToUnload, QP_HT);
			Qunloaded = Math.min(Qunloaded, project.heatNet.maximumPerformance);
			QP_HT = QP_HT - Qunloaded;
		}
		else
		{
			Qunloaded = Math.min(qToUnload, QP_NT);
			Qunloaded = Math.min(Qunloaded, project.heatNet.maximumPerformance);
			QP_NT = QP_NT - Qunloaded;
		}
		
		double qToUnloadRemaining = qToUnload - Qunloaded;

		log.beginProducer(null);
		log.message(String.format("BufferCalcState.unload(%s, %f, %s) -> unloaded %f, still to unload %f", hour, qToUnload, isHT ? "HT" : "NT", Qunloaded, qToUnloadRemaining));

		UpdateFGAndTE();

		return qToUnloadRemaining;
	}
	
	public void applyLoss(int hour, double qLoss)
	{
		double Qlost = Math.min(qLoss, QP_HT);
		QP_HT = QP_HT - Qlost;
		
		double qLossRemaining = qLoss - Qlost;
		
		if(qLossRemaining > 0)
		{
			Qlost = qLoss;
			QP_NT = QP_NT - Qlost;
		}
		
		log.beginProducer(null);
		log.message(String.format("BufferCalcState.applyLoss(%s, %f) -> lost %f", hour, qLoss, Qlost));

		UpdateFGAndTE();
	}

	public void postStep(int hour) 
	{
		log.beginProducer(null);
		log.hourValues(hour, QP_MAX, QP_100, QP_HT, QP_NT, TV, TR, FG, TE);
	}
	
	public double totalUnloadablePower()
	{
		return QP_HT + QP_NT;
	}
	
	public double getloadFactor()
	{
		return (QP_HT + QP_NT) / (QP_MAX * maxTargetLoadFactor);
	}

	public double CalcHTCapacity()
	{
		return Math.max(0, QP_MAX * maxTargetLoadFactor - QP_HT - QP_NT);
	}
	
	public double CalcNTCapacity()
	{
	    //return Math.max(0, (1 - QP_HT / QP_100) * QP_100);
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
}
