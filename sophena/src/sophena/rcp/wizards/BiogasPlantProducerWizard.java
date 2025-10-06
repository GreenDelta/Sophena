package sophena.rcp.wizards;

import java.util.ArrayList;
import java.util.UUID;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.calc.biogas.BiogasPlantResult;
import sophena.db.daos.ProjectDao;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.editors.producers.ProducerEditor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Num;
import sophena.utils.Strings;

public class BiogasPlantProducerWizard extends Wizard {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Project project;
	private Page page;

	public static void open(ProjectDescriptor d) {
		if (d == null)
			return;
		var dao = new ProjectDao(App.getDb());
		open(dao.get(d.id));
	}

	public static void open(Project project) {
		if (project == null)
			return;
		var wiz = new BiogasPlantProducerWizard(project);
		wiz.setWindowTitle("Biogasanlage hinzufügen");
		var dialog = new WizardDialog(UI.shell(), wiz);
		dialog.setPageSize(150, 450);
		if (dialog.open() == Window.OK) {
			Navigator.refresh();
		}
	}

	private BiogasPlantProducerWizard(Project project) {
		this.project = project;
	}

	@Override
	public boolean performFinish() {
		try {
			Producer producer = new Producer();
			producer.id = UUID.randomUUID().toString();
			page.bindToModel(producer);
			Wizards.initFuelSpec(producer, project);
			Wizards.initCosts(producer);
			Wizards.initElectricity(producer, project);
			project.producers.add(producer);
			ProjectDao dao = new ProjectDao(App.getDb());
			dao.update(project);
			Navigator.refresh();
			ProducerEditor.open(project.toDescriptor(), producer.toDescriptor());
			return true;
		} catch (Exception e) {
			log.error("failed to update project with new biogas plant producer", e);
			return false;
		}
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private Text nameText;
		private boolean nameEdited;
		private TableViewer biogasPlantTable;
		private Text rankText;
		private Combo functionCombo;

		Page() {
			super("BiogasPlantPage", "Biogasanlage hinzufügen", null);
			setMessage(" ");
		}

		@Override
		public void createControl(Composite parent) {
			Composite root = new Composite(parent, SWT.NONE);
			setControl(root);
			UI.gridLayout(root, 1, 5, 5);

			Composite comp = UI.formComposite(root);
			UI.gridData(comp, true, false);
			nameField(comp);

			biogasPlantTable(root);
			functionFields(root);
			bindToUI();
		}

		private void nameField(Composite comp) {
			nameText = UI.formText(comp, M.Name);
			nameEdited = false;
			// smart identification if the name was edited by the user
			Texts.on(nameText).required().onChanged((t) -> {
				BiogasPlant plant = Viewers.getFirstSelected(biogasPlantTable);
				if (plant == null) {
					nameEdited = true;
				} else {
					nameEdited = !Strings.nullOrEqual(t, plant.name);
				}
			});
		}

		private void biogasPlantTable(Composite root) {
			biogasPlantTable = Tables.createViewer(root, "Name", "Kessel", "Bemessungsleistung");
			Tables.bindColumnWidths(biogasPlantTable, 0.4, 0.4, 0.2);
			biogasPlantTable.setContentProvider(ArrayContentProvider.getInstance());
			biogasPlantTable.setLabelProvider(new BiogasPlantLabel());
			biogasPlantTable.addSelectionChangedListener((e) -> {
				suggestName();
				validate();
			});
			updateBiogasPlants();
		}

		private void functionFields(Composite root) {
			Composite composite = new Composite(root, SWT.NONE);
			UI.gridLayout(composite, 4);
			UI.gridData(composite, true, false);
			rankText = UI.formText(composite, "Rang");
			Texts.on(rankText).integer().required().validate(this::validate);
			functionCombo = UI.formCombo(composite, M.BufferTank);
		}

		private void bindToModel(Producer p) {
			if (p == null)
				return;
			BiogasPlant plant = Viewers.getFirstSelected(biogasPlantTable);
			p.biogasPlant = plant;
			if (plant != null) {
				p.productGroup = plant.productGroup;
				var r = BiogasPlantResult.calculate(plant);
				p.profile = r.asProducerProfile();
				p.profileMaxPower = plant.product != null
					? plant.product.maxPower
					: Stats.max(p.profile.maxPower);
				p.profileMaxPowerElectric = plant.product != null
					? plant.product.maxPowerElectric
					: 0;
			}
			p.name = nameText.getText();
			p.rank = Texts.getInt(rankText);
			p.function = Wizards.getProducerFunction(functionCombo);
		}

		private void bindToUI() {
			Texts.set(rankText, Wizards.nextProducerRank(project));
			Wizards.fillProducerFunctions(project, functionCombo);
			if (functionCombo.getItemCount() > 0)
				functionCombo.select(0);
			setPageComplete(false);
		}

		private void suggestName() {
			if (nameEdited && !Texts.isEmpty(nameText))
				return;
			BiogasPlant plant = Viewers.getFirstSelected(biogasPlantTable);
			if (plant == null) {
				nameText.setText("");
			} else {
				Texts.set(nameText, plant.name);
			}
		}

		private void updateBiogasPlants() {
			var input = new ArrayList<BiogasPlant>();
			for (BiogasPlant plant : App.getDb().getAll(BiogasPlant.class)) {
				input.add(plant);
			}
			Sorters.byName(input);
			biogasPlantTable.setInput(input);
			setPageComplete(false);
		}

		private boolean validate() {
			if (!Texts.hasNumber(rankText))
				return err("Der Rang muss ein numerischer Wert sein");
			int rank = Texts.getInt(rankText);
			if (Wizards.producerRankExists(project, rank)) {
				return err("Es besteht bereits ein Wärmeerzeuger mit"
						+ " dem angegebenen Rang.");
			}
			setErrorMessage(null);
			if (Viewers.getFirstSelected(biogasPlantTable) == null) {
				setPageComplete(false);
				return false;
			}
			setPageComplete(true);
			return true;
		}

		private boolean err(String msg) {
			setPageComplete(false);
			setErrorMessage(msg);
			return false;
		}
	}

	private static class BiogasPlantLabel extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof BiogasPlant plant))
				return null;
			return switch (col) {
				case 0 -> plant.name;
				case 1 -> plant.product != null ? plant.product.name : null;
				case 2 -> Num.str(plant.ratedPower) + " kW";
				default -> null;
			};
		}
	}
}
