package sophena.rcp.wizards;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sophena.db.Database;
import sophena.db.daos.Dao;
import sophena.model.BuildingType;
import sophena.model.Consumer;
import sophena.model.Project;
import sophena.model.RootEntity;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.editors.consumers.ConsumerEditor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.UI;

public class ConsumerWizard extends Wizard implements INewWizard {

	private static final String ID = "sophena.ConsumerWizard";

	private Logger log = LoggerFactory.getLogger(getClass());
	private Page page;
	private Project project;

	public static void open(Project project) {
		try {
			ConsumerWizard wizard = new ConsumerWizard();
			wizard.setWindowTitle(M.CreateNewConsumer);
			wizard.project = project;
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
			// TODO: save consumer
			ConsumerEditor.open(page.consumer);
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

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	private class Page extends WizardPage {

		private Consumer consumer;
		private List<BuildingType> types;
		private String[] typeNames;

		private Page() {
			super("ConsumerWizardPage", M.CreateNewConsumer, null);
			initData();
		}

		private void initData() {
			consumer = new Consumer();
			consumer.setId(UUID.randomUUID().toString());
			consumer.setName(M.NewConsumer);
			try {
				Database db = App.getDb();
				Dao<BuildingType> typeDao = new Dao<>(BuildingType.class, db);
				types = typeDao.getAll();
				typeNames = getNames(types);
				if(!types.isEmpty())
					consumer.setBuildingType(types.get(0));
			} catch (Exception e) {
				types = Collections.emptyList();
				typeNames = new String[0];
				log.error("failed to load building types / states", e);
			}
		}

		private String[] getNames(List<? extends RootEntity> list) {
			Collections.sort(list, (e1, e2)
					-> Strings.compare(e1.getName(), e2.getName()));
			String[] names = new String[list.size()];
			for(int i = 0; i < list.size(); i++){
				names[i] = list.get(i).getName();
			}
			return names;
		}

		@Override
		public void createControl(Composite parent) {
			Composite composite = UI.formComposite(parent);
			setControl(composite);
			Text nameText = UI.formText(composite, M.Name);
			nameText.setText(consumer.getName());
			nameText.addModifyListener((e) -> {
				consumer.setName(nameText.getText());
				validate();
			});
			Text descriptionText = UI.formMultiText(composite, M.Description);
			descriptionText.addModifyListener((e) -> {
				consumer.setDescription(descriptionText.getText());
			});
			Combo typeCombo = UI.formCombo(composite, M.BuildingType);
			typeCombo.setItems(typeNames);
			if(typeNames.length > 0)
				typeCombo.select(0);
			Combo stateCombo = UI.formCombo(composite, M.BuildingState);
		}

		private void validate() {
			// TODO implement validation function
		}
	}

}
