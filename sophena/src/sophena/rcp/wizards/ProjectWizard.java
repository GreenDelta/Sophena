package sophena.rcp.wizards;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.CostSettingsDao;
import sophena.db.daos.ProjectDao;
import sophena.db.daos.WeatherStationDao;
import sophena.model.CostSettings;
import sophena.model.HeatNet;
import sophena.model.Project;
import sophena.model.WeatherStation;
import sophena.model.descriptors.WeatherStationDescriptor;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.editors.projects.ProjectEditor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

public class ProjectWizard extends Wizard {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Page page;

	public static void open() {
		try {
			ProjectWizard wizard = new ProjectWizard();
			wizard.setWindowTitle(M.CreateNewProject);
			WizardDialog dialog = new WizardDialog(UI.shell(), wizard);
			dialog.setPageSize(150, 350);
			if (dialog.open() == Window.OK)
				Navigator.refresh();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(ProjectWizard.class);
			log.error("failed to create project", e);
		}
	}

	@Override
	public boolean performFinish() {
		try {
			Project p = new Project();
			p.id = UUID.randomUUID().toString();
			page.data.bindToModel(p);
			addHeatNet(p);
			addCostSettings(p);
			ProjectDao dao = new ProjectDao(App.getDb());
			dao.insert(p);
			Navigator.refresh();
			ProjectEditor.open(p.toDescriptor());
			return true;
		} catch (Exception e) {
			log.error("failed to create project", e);
			return false;
		}
	}

	private void addCostSettings(Project p) {
		CostSettingsDao dao = new CostSettingsDao(App.getDb());
		CostSettings global = dao.getGlobal();
		if (global != null)
			p.setCostSettings(global.clone());
		else {
			CostSettings settings = new CostSettings();
			settings.id = UUID.randomUUID().toString();
			p.setCostSettings(settings);
		}
	}

	private void addHeatNet(Project p) {
		HeatNet n = p.getHeatNet();
		n.simultaneityFactor = (double) 1;
		n.powerLoss = (double) 20;
		n.supplyTemperature = (double) 80;
		n.returnTemperature = (double) 50;
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private DataBinding data = new DataBinding();

		private Text nameText;
		private Text descriptionText;
		private Text timeText;
		private EntityCombo<WeatherStationDescriptor> stationCombo;

		protected Page() {
			super("ProjectWizardPage", M.CreateNewProject, null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite composite = UI.formComposite(parent);
			setControl(composite);
			nameText = UI.formText(composite, M.Name);
			Texts.on(nameText).required().validate(data::validate);
			descriptionText = UI.formMultiText(composite, M.Description);
			timeText = UI.formText(composite, M.ProjectDurationYears);
			Texts.on(timeText).required().integer().validate(data::validate);
			stationCombo = new EntityCombo<>();
			stationCombo.create("Wetterstation", composite);
			stationCombo.onSelect((d) -> data.validate());
			data.bindToUI();
		}

		private class DataBinding {

			void bindToModel(Project p) {
				if (p == null)
					return;
				p.name = nameText.getText();
				p.description = descriptionText.getText();
				p.setProjectDuration(Texts.getInt(timeText));
				p.setWeatherStation(getWeatherStation());
			}

			private WeatherStation getWeatherStation() {
				WeatherStationDescriptor d = stationCombo.getSelected();
				if (d == null)
					return null;
				WeatherStationDao dao = new WeatherStationDao(App.getDb());
				return dao.get(d.id);
			}

			void bindToUI() {
				nameText.setText(M.NewProject);
				Texts.set(timeText, 20);
				initWeatherStations();
				validate();
			}

			private void initWeatherStations() {
				WeatherStationDao dao = new WeatherStationDao(App.getDb());
				List<WeatherStationDescriptor> list = dao.getDescriptors();
				Collections.sort(list, (w1, w2) -> Strings.compare(w1.name,
						w2.name));
				stationCombo.setInput(list);
				if (!list.isEmpty())
					stationCombo.select(list.get(0));
				else
					setPageComplete(false);
			}

			private boolean validate() {
				if (Texts.isEmpty(nameText))
					return error("Es muss ein Name angegeben werden.");
				int time = Texts.getInt(timeText);
				if (time < 1 || time > 1000)
					return error(
							"Die Projektlaufzeit enthält keinen gültigen Wert.");
				WeatherStationDescriptor d = stationCombo.getSelected();
				if (d == null)
					return error("Es wurde keine Wetterstation ausgewählt.");
				setErrorMessage(null);
				setPageComplete(true);
				return true;
			}

			private boolean error(String message) {
				setErrorMessage(message);
				setPageComplete(false);
				return false;
			}
		}
	}
}
