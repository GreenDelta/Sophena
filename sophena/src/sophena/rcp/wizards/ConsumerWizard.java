package sophena.rcp.wizards;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.Labels;
import sophena.db.daos.BuildingStateDao;
import sophena.db.daos.ProjectDao;
import sophena.model.BuildingState;
import sophena.model.BuildingType;
import sophena.model.Consumer;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.colors.Colors;
import sophena.rcp.editors.consumers.ConsumerEditor;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpLink;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.UI;
import sophena.utils.Num;
import sophena.utils.Strings;

public class ConsumerWizard extends Wizard {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Page page;
	private Project project;

	public static void open(ProjectDescriptor d) {
		if (d == null)
			return;
		ProjectDao dao = new ProjectDao(App.getDb());
		open(dao.get(d.id));
	}

	public static void open(Project project) {
		if (project == null)
			return;
		ConsumerWizard wiz = new ConsumerWizard();
		wiz.setWindowTitle(M.CreateNewConsumer);
		wiz.project = project;
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		if (dialog.open() == Window.OK)
			Navigator.refresh();
	}

	@Override
	public boolean performFinish() {
		try {
			Consumer consumer = page.consumer;
			setDefaults(consumer);
			project.consumers.add(consumer);
			ProjectDao dao = new ProjectDao(App.getDb());
			dao.update(project);
			Navigator.refresh();
			ConsumerEditor.open(project.toDescriptor(),
					consumer.toDescriptor());
			return true;
		} catch (Exception e) {
			log.error("failed to save consumer", e);
			return false;
		}
	}

	private void setDefaults(Consumer c) {
		BuildingState bs = c.buildingState;
		if (bs != null) {
			c.heatingLimit = bs.heatingLimit;
			c.waterFraction = bs.waterFraction;
			c.loadHours = bs.loadHours;
		} else {
			c.heatingLimit = 15;
			c.waterFraction = 10;
			c.loadHours = 1800;
		}
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private Consumer consumer;
		private EntityCombo<BuildingState> stateCombo;

		private Page() {
			super("ConsumerWizardPage", M.CreateNewConsumer, null);
			setMessage(" ");
			consumer = new Consumer();
			consumer.id = UUID.randomUUID().toString();
			consumer.name = M.NewConsumer;
			consumer.demandBased = false;
		}

		@Override
		public void createControl(Composite parent) {
			Composite composite = UI.formComposite(parent);
			setControl(composite);
			Text nt = UI.formText(composite, M.Name);
			nt.setBackground(Colors.forRequiredField());
			nt.setText(consumer.name);
			nt.addModifyListener(e -> {
				consumer.name = nt.getText();
				validate();
			});
			Text dt = UI.formMultiText(composite, M.Description);
			dt.addModifyListener((e) -> consumer.description = dt.getText());
			createTypeCombo(composite);
			createStateCombo(composite);
			createCalculationRadios(composite);
		}

		private void createTypeCombo(Composite composite) {
			Combo combo = UI.formCombo(composite, M.BuildingType);
			BuildingType[] types = buildingTypes();
			String[] items = new String[types.length];
			for (int i = 0; i < types.length; i++) {
				items[i] = Labels.get(types[i]);
			}
			combo.setItems(items);
			combo.select(0);
			Controls.onSelect(combo, e -> {
				int idx = combo.getSelectionIndex();
				initBuildingState(types[idx]);
			});
		}

		/** Get the building types that have at least one building state. */
		private BuildingType[] buildingTypes() {
			BuildingStateDao dao = new BuildingStateDao(App.getDb());
			BuildingType[] types = BuildingType.values();
			Set<BuildingType> used = new HashSet<>();
			for (BuildingState s : dao.getAll()) {
				if (s.type == null)
					continue;
				used.add(s.type);
			}
			BuildingType[] selected = new BuildingType[used.size()];
			int i = 0;
			for (BuildingType type : types) {
				if (!used.contains(type) || i >= selected.length)
					continue;
				selected[i] = type;
				i++;
			}
			return selected;
		}

		private void createStateCombo(Composite composite) {
			stateCombo = new EntityCombo<>();
			stateCombo.create(M.BuildingState, composite);
			initBuildingState(BuildingType.values()[0]);
			stateCombo.onSelect(s -> consumer.buildingState = s);
		}

		private void initBuildingState(BuildingType type) {
			BuildingStateDao dao = new BuildingStateDao(App.getDb());
			List<BuildingState> states = dao.getAllWith(type);
			BuildingState selected = BuildingStateDao.getDefault(states);
			stateCombo.setInput(states);
			stateCombo.select(selected);
			consumer.buildingState = selected;
		}

		private void createCalculationRadios(Composite composite) {
			UI.formLabel(composite, "");
			Button consumption = new Button(composite, SWT.RADIO);
			consumption.setText(M.ConsumptionBasedCalculation);
			consumption.setSelection(true);
			UI.formLabel(composite, "");
			Button demand = new Button(composite, SWT.RADIO);
			demand.setText(M.DemandBasedCalculation);
			UI.formLabel(composite, "");
			Composite inner = new Composite(composite, SWT.NONE);
			UI.innerGrid(inner, 3);
			Text text = UI.formText(inner, M.HeatingLoad + " (kW)");
			text.addModifyListener(
					(e) -> consumer.heatingLoad = Num.read(text.getText()));
			HelpLink.create(inner, M.HeatingLoad, H.HeatingLoad);
			inner.setVisible(false);
			Controls.onSelect(consumption, (e) -> {
				consumer.demandBased = false;
				inner.setVisible(false);
			});
			Controls.onSelect(demand, (e) -> {
				consumer.demandBased = true;
				inner.setVisible(true);
			});
		}

		private void validate() {
			if (Strings.nullOrEmpty(consumer.name)) {
				setErrorMessage(M.NoEmptyNameAllowed);
				setPageComplete(false);
			} else {
				setErrorMessage(null);
				setPageComplete(true);
			}
		}
	}

}
