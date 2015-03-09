package sophena.rcp.wizards;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sophena.db.daos.Dao;
import sophena.db.daos.ProjectDao;
import sophena.model.BuildingState;
import sophena.model.BuildingType;
import sophena.model.Consumer;
import sophena.model.Project;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.Numbers;
import sophena.rcp.editors.consumers.ConsumerEditor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.UI;

public class ConsumerWizard extends Wizard {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Page page;
	private Project project;

	public static void open(Project project) {
		ConsumerWizard wiz = new ConsumerWizard();
		wiz.setWindowTitle(M.CreateNewConsumer);
		wiz.project = project;
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		dialog.setPageSize(150, 400);
		if (dialog.open() == Window.OK)
			Navigator.refresh();
	}

	@Override
	public boolean performFinish() {
		try {
			Consumer consumer = page.consumer;
			project.getConsumers().add(consumer);
			ProjectDao dao = new ProjectDao(App.getDb());
			dao.update(project);
			Navigator.refresh(project);
			ConsumerEditor.open(project, consumer);
			return true;
		} catch (Exception e) {
			log.error("failed to save consumer", e);
			return false;
		}
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private Consumer consumer;

		private Page() {
			super("ConsumerWizardPage", M.CreateNewConsumer, null);
			consumer = new Consumer();
			consumer.setId(UUID.randomUUID().toString());
			consumer.setName(M.NewConsumer);
			consumer.setDemandBased(false);
		}

		@Override
		public void createControl(Composite parent) {
			Composite composite = UI.formComposite(parent);
			setControl(composite);
			Text nt = UI.formText(composite, M.Name);
			nt.setBackground(Colors.forRequiredField());
			nt.setText(consumer.getName());
			nt.addModifyListener((e) -> {
				consumer.setName(nt.getText());
				validate();
			});
			Text dt = UI.formMultiText(composite, M.Description);
			dt.addModifyListener((e) -> consumer.setDescription(dt.getText()));
			createTypeCombo(composite);
			createStateCombo(composite);
			createCalculationRadios(composite);
		}

		private void createTypeCombo(Composite composite) {
			EntityCombo<BuildingType> combo = new EntityCombo<>();
			combo.create(M.BuildingType, composite);
			Dao<BuildingType> dao = new Dao<>(BuildingType.class, App.getDb());
			List<BuildingType> types = dao.getAll();
			if (types.isEmpty())
				return;
			Collections.sort(types, (e1, e2)
					-> Strings.compare(e1.getName(), e2.getName()));
			combo.setInput(types);
			combo.select(types.get(0));
			consumer.setBuildingType(types.get(0));
			combo.onSelect(consumer::setBuildingType);
		}

		private void createStateCombo(Composite composite) {
			EntityCombo<BuildingState> combo = new EntityCombo<>();
			combo.create(M.BuildingState, composite);
			Dao<BuildingState> dao = new Dao<>(BuildingState.class, App.getDb());
			List<BuildingState> states = dao.getAll();
			if (states.isEmpty())
				return;
			Collections.sort(states, (s1, s2)
					-> Strings.compare(s1.getName(), s2.getName()));
			combo.setInput(states);
			combo.select(states.get(0));
			consumer.setBuildingState(states.get(0));
			combo.onSelect(consumer::setBuildingState);
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
			UI.innerGrid(inner, 2);
			Text text = UI.formText(inner, M.HeatingLoad);
			text.addModifyListener((e) ->
				consumer.setHeatingLoad(Numbers.read(text.getText())));
			inner.setVisible(false);
			Controls.onSelect(consumption, (e) -> {
				consumer.setDemandBased(false);
				inner.setVisible(false);
			});
			Controls.onSelect(demand, (e) -> {
				consumer.setDemandBased(true);
				inner.setVisible(true);
			});
		}

		private void validate() {
			if (Strings.nullOrEmpty(consumer.getName())) {
				setErrorMessage(M.NoEmptyNameAllowed);
				setPageComplete(false);
			} else {
				setErrorMessage(null);
				setPageComplete(true);
			}
		}
	}

}
