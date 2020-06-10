package sophena.rcp.wizards;

import java.util.List;
import java.util.UUID;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.Defaults;
import sophena.db.daos.CostSettingsDao;
import sophena.db.daos.ProjectDao;
import sophena.db.daos.WeatherStationDao;
import sophena.model.CostSettings;
import sophena.model.HeatNet;
import sophena.model.Project;
import sophena.model.WeatherStation;
import sophena.model.descriptors.WeatherStationDescriptor;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.editors.basedata.climate.ClimateStationBrowser;
import sophena.rcp.editors.projects.ProjectEditor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

public class ProjectWizard extends Wizard {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private Page page;

	public static void open() {
		try {
			var wizard = new ProjectWizard();
			wizard.setWindowTitle(M.CreateNewProject);
			var dialog = new WizardDialog(UI.shell(), wizard);
			if (dialog.open() == Window.OK)
				Navigator.refresh();
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(ProjectWizard.class);
			log.error("failed to create project", e);
		}
	}

	@Override
	public boolean performFinish() {
		try {
			var project = new Project();
			project.id = UUID.randomUUID().toString();
			page.data.bindToModel(project);
			addHeatNet(project);
			addCostSettings(project);
			ProjectDao dao = new ProjectDao(App.getDb());
			dao.insert(project);
			Navigator.refresh();
			ProjectEditor.open(project.toDescriptor());
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
			p.costSettings = global.clone();
		else {
			CostSettings settings = new CostSettings();
			settings.id = UUID.randomUUID().toString();
			p.costSettings = settings;
		}
	}

	private void addHeatNet(Project p) {
		var net = (p.heatNet = new HeatNet());
		net.id = UUID.randomUUID().toString();
		net.simultaneityFactor = 1;
		net.powerLoss = 20;
		net.maxBufferLoadTemperature = 95;
		net.bufferLambda = 0.04;
		net.supplyTemperature = 80;
		net.returnTemperature = 50;
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private static class Page extends WizardPage {

		private final DataBinding data = new DataBinding();

		private Text nameText;
		private Text descriptionText;
		private Text timeText;
		private EntityCombo<WeatherStationDescriptor> stationCombo;

		protected Page() {
			super("ProjectWizardPage", M.CreateNewProject, null);
			setMessage(" ");
		}

		@Override
		public void createControl(Composite parent) {
			Composite composite = UI.formComposite(parent);
			UI.gridLayout(composite, 3);
			setControl(composite);
			nameText = UI.formText(composite, M.Name);
			Texts.on(nameText).required().validate(data::validate);
			UI.filler(composite);
			descriptionText = UI.formMultiText(composite, M.Description);
			UI.filler(composite);
			timeText = UI.formText(composite, M.ProjectDurationYears);
			Texts.on(timeText).required().integer().validate(data::validate);
			UI.filler(composite);
			stationCombo = new EntityCombo<>();
			stationCombo.create("Wetterstation", composite);
			stationCombo.onSelect(d -> data.validate());
			ImageHyperlink link = new ImageHyperlink(composite, SWT.NONE);
			link.setImage(Icon.SEARCH_16.img());
			Controls.onClick(link, e -> openMap());
			data.bindToUI();
		}

		private void openMap() {
			Shell shell = new Shell(UI.shell());
			Monitor monitor = UI.shell().getMonitor();
			Rectangle outer = monitor.getBounds();
			int width = (int) (outer.width * 0.8);
			int height = (int) (outer.height * 0.8);
			shell.setSize(width, height);
			int x = outer.x + (outer.width - width) / 2;
			int y = outer.y + (outer.height - height) / 2;
			shell.setLocation(x, y);
			ClimateStationBrowser.create(shell);
			shell.open();
		}

		private class DataBinding {

			void bindToModel(Project p) {
				if (p == null)
					return;
				p.name = nameText.getText();
				p.description = descriptionText.getText();
				p.duration = Texts.getInt(timeText);
				p.weatherStation = getWeatherStation();
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
				Sorters.byName(list);
				stationCombo.setInput(list);
				if (!list.isEmpty())
					stationCombo.select(list.get(0));
				else
					setPageComplete(false);
			}

			private void validate() {
				if (Texts.isEmpty(nameText)) {
					error("Es muss ein Name angegeben werden.");
					return;
				}
				int time = Texts.getInt(timeText);
				if (time < 1 || time > 1000) {
					error("Die Projektlaufzeit enthält keinen gültigen Wert.");
					return;
				}
				WeatherStationDescriptor d = stationCombo.getSelected();
				if (d == null) {
					error("Es wurde keine Wetterstation ausgewählt.");
					return;
				}
				setErrorMessage(null);
				setPageComplete(true);
			}

			private void error(String message) {
				setErrorMessage(message);
				setPageComplete(false);
			}
		}
	}
}
