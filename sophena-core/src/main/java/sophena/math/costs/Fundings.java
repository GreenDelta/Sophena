package sophena.math.costs;

import sophena.calc.CalcLog;
import sophena.model.Consumer;
import sophena.model.Producer;
import sophena.model.ProductCosts;
import sophena.model.ProductType;
import sophena.model.Project;

public class Fundings {

	public static double get(Project project, CalcLog log) {
		if (project == null || project.costSettings == null)
			return 0;
		if (log != null) {
			log.h3("Förderung");
			log.value("Investitionsförderung allg.",
					project.costSettings.funding, "EUR");
		}
		double total = project.costSettings.funding
				+ getForTransferStations(project, log)
				+ getForBiomassBoilers(project, log)
				+ getForHeatNet(project, log);
		if (log != null) {
			log.value("Förderung insgesamt", total, "EUR");
			log.println();
		}
		return total;
	}

	private static double getForBiomassBoilers(Project project, CalcLog log) {
		double f = project.costSettings.fundingBiomassBoilers;
		if (f <= 0)
			return 0;
		double kWs = 0;
		for (Producer p : project.producers) {
			if (p.disabled || p.productGroup == null)
				continue;
			ProductType type = p.productGroup.type;
			if (p.boiler != null && type == ProductType.BIOMASS_BOILER) {
				kWs += p.boiler.maxPower;
			}
		}
		double total = kWs * f;
		if (log != null) {
			log.value("Förderung Biomassekessel pro kW", f, "EUR/kW");
			log.value("Leistung der Biomassekessel insg.", kWs, "kW");
			log.value("Förderung Biomassekessel insg.", total, "EUR");
		}
		return total;
	}

	private static double getForHeatNet(Project project, CalcLog log) {
		double f = project.costSettings.fundingHeatNet; // EUR/m
		if (f <= 0)
			return 0;
		double total = project.heatNet.length * f;
		if (log != null) {
			log.value("Förderung Wärmenetz pro m", f, "EUR/m");
			log.value("Länge des Heiznetzes", project.heatNet.length, "m");
			log.value("Förderung Wärmenetz insg.", total, "EUR");
		}
		return total;
	}

	private static double getForTransferStations(Project project, CalcLog log) {
		double f = project.costSettings.fundingTransferStations;
		if (f <= 0)
			return 0;
		double count = 0;
		for (Consumer c : project.consumers) {
			if (c.disabled)
				continue;
			if (c.transferStation != null) {
				count += 1.0;
				continue;
			}
			ProductCosts cost = c.transferStationCosts;
			if (cost != null && cost.investment > 0) {
				count += 1.0;
				continue;
			}
		}
		double total = f * count;
		if (log != null) {
			log.value("Förderung Hausübergabestationen pro Stk.", f, "EUR/Stk");
			log.value("Anzahl der Hausübergabestationen", count, "Stk");
			log.value("Förderung Hausübergabestationen insg.", total, "EUR");
		}
		return total;
	}
}
