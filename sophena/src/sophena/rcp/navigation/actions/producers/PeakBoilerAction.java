package sophena.rcp.navigation.actions.producers;

import java.util.List;

import sophena.calc.ProjectLoad;
import sophena.db.daos.ProducerDao;
import sophena.db.daos.ProjectDao;
import sophena.model.HoursTrace;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.SubFolderElement;
import sophena.rcp.navigation.SubFolderType;
import sophena.rcp.navigation.actions.NavigationAction;
import sophena.rcp.utils.MsgBox;
import sophena.utils.Num;

/// An action for estimating the peak-load boiler of a project.
public class PeakBoilerAction extends NavigationAction {

	private ProjectDescriptor project;

	public PeakBoilerAction() {
		setText("Spitzenlastkessel abschätzen");
		setImageDescriptor(Icon.BOILER_16.des());
	}

	@Override
	public boolean accept(List<NavigationElement> list) {
		if (list == null || list.size() != 1)
			return false;
		if (!(list.getFirst() instanceof SubFolderElement folder))
			return false;
		if (folder.getType() != SubFolderType.PRODUCTION)
			return false;

		var project = folder.getProject();
		if (project == null)
			return false;

		var dao = new ProducerDao(App.getDb());
		if (dao.getDescriptors(project).isEmpty())
			return false;

		this.project = project;
		return true;
	}

	@Override
	public void run() {
		if (project == null)
			return;

		var project = new ProjectDao(App.getDb()).get(this.project.id);
		if (project == null)
			return;

		double demand = estimateDemand(project);

		MsgBox.info(
			"Spitzenlastkessel abschätzen",
			"Vorläufige Abschätzung der benötigten Spitzenlastkessel-Leistung: "
				+ Num.str(demand) + " kW. Die konkrete Folgeaktion wird "
				+ "im nächsten Schritt ergänzt.");
	}

	static double estimateDemand(Project project) {
		if (project == null)
			return 0;

		var curve = ProjectLoad.getSmoothedCurve(project);
		double maxDiff = 0;
		for (int hour = 0; hour < Stats.HOURS; hour++) {
			double load = Stats.get(curve, hour);
			double provided = 0;
			for (var producer : project.producers) {
				provided += powerOf(producer, project, hour);
			}
			double diff = Math.max(0, load - provided);
			maxDiff = Math.max(maxDiff, diff);
		}
		return Math.ceil(maxDiff);
	}

	private static double powerOf(Producer producer, Project project, int hour) {
		if (producer == null || producer.disabled)
			return 0;
		if (isInterrupted(producer, hour))
			return 0;
		if (isOutdoorTemperatureControlledOff(producer, project, hour))
			return 0;
		if (producer.solarCollector != null)
			return 0;
		if (producer.hasProfile())
			return securedProfilePower(producer);
		if (producer.heatPump != null)
			return Math.max(0, producer.heatPump.ratedPower);
		if (producer.boiler != null)
			return Math.max(0, producer.boiler.maxPower);
		return 0;
	}

	private static double securedProfilePower(Producer producer) {
		if (producer == null || producer.profile == null)
			return Math.max(0, producer != null ? producer.profileMaxPower : 0);
		return Math.max(0, Stats.min(producer.profile.maxPower));
	}

	private static boolean isInterrupted(Producer producer, int hour) {
		if (producer == null)
			return false;
		for (var interruption : producer.interruptions) {
			var interval = HoursTrace.getDayInterval(interruption);
			if (interval.length < 2)
				continue;
			int start = interval[0];
			int end = interval[1];
			if (start <= end) {
				if (hour >= start && hour <= end)
					return true;
			} else if (hour >= start || hour <= end) {
				return true;
			}
		}
		return false;
	}

	private static boolean isOutdoorTemperatureControlledOff(
			Producer producer, Project project, int hour) {
		if (producer == null || !producer.isOutdoorTemperatureControl)
			return false;
		if (project == null || project.weatherStation == null || project.weatherStation.data == null)
			return false;

		double temperature = Stats.get(project.weatherStation.data, hour);
		return switch (producer.outdoorTemperatureControlKind) {
			case From -> producer.outdoorTemperature > temperature;
			case Until -> producer.outdoorTemperature < temperature;
			case null -> false;
		};
	}
}
