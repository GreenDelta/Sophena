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

import sophena.calc.biogas.BiogasPlants;
import sophena.db.daos.ProjectDao;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.biogas.BiogasPlant;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.M;
import sophena.rcp.app.App;
import sophena.rcp.editors.producers.ProducerEditor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Num;
import sophena.utils.Producers;
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
			page.bindToModel(producer, project);
			Producers.initFuelAndCosts(producer, project);
			var dao = new ProjectDao(App.getDb());
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
		private TableViewer table;
		private Text rankText;
		private Combo functionCombo;

		Page() {
			super("BiogasPlantPage", "Biogasanlage hinzufügen", null);
			setMessage(" ");
		}

		@Override
		public void createControl(Composite parent) {
			var root = new Composite(parent, SWT.NONE);
			setControl(root);
			UI.gridLayout(root, 1, 5, 5);
			var comp = UI.formComposite(root);
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
				BiogasPlant plant = Viewers.getFirstSelected(table);
				if (plant == null) {
					nameEdited = true;
				} else {
					nameEdited = !Strings.nullOrEqual(t, plant.name);
				}
			});
		}

		private void biogasPlantTable(Composite root) {
			table = Tables.createViewer(root, "Name", "Kessel", "Bemessungsleistung");
			Tables.bindColumnWidths(table, 0.4, 0.4, 0.2);
			table.setContentProvider(ArrayContentProvider.getInstance());
			table.setLabelProvider(new TableLabel());
			table.addSelectionChangedListener((e) -> {
				suggestName();
				validate();
			});
			updateBiogasPlants();
		}

		private void functionFields(Composite root) {
			var composite = new Composite(root, SWT.NONE);
			UI.gridLayout(composite, 4);
			UI.gridData(composite, true, false);
			rankText = UI.formText(composite, "Rang");
			Texts.on(rankText).integer().required().validate(this::validate);
			functionCombo = UI.formCombo(composite, M.BufferTank);
		}

		private void bindToModel(Producer p, Project project) {
			if (p == null)
				return;
			p.biogasPlant = Viewers.getFirstSelected(table);
			BiogasPlants.syncProducerProfile(project, p);
			p.name = nameText.getText();
			p.rank = Texts.getInt(rankText);
			p.function = Wizards.getProducerFunction(functionCombo);
			project.producers.add(p);
		}

		private void bindToUI() {
			Texts.set(rankText, Wizards.nextProducerRank(project));
			Wizards.fillProducerFunctions(project, functionCombo);
			if (functionCombo.getItemCount() > 0) {
				functionCombo.select(0);
			}
			setPageComplete(false);
		}

		private void suggestName() {
			if (nameEdited && !Texts.isEmpty(nameText))
				return;
			BiogasPlant plant = Viewers.getFirstSelected(table);
			if (plant == null) {
				nameText.setText("");
			} else {
				Texts.set(nameText, plant.name);
			}
		}

		private void updateBiogasPlants() {
			var input = new ArrayList<>(App.getDb().getAll(BiogasPlant.class));
			Sorters.byName(input);
			table.setInput(input);
			setPageComplete(false);
		}

		private void validate() {
			if (!Texts.hasNumber(rankText)) {
				err("Der Rang muss ein numerischer Wert sein");
				return;
			}
			int rank = Texts.getInt(rankText);
			if (Wizards.producerRankExists(project, rank)) {
				err("Es besteht bereits ein Wärmeerzeuger mit"
					+ " dem angegebenen Rang.");
				return;
			}
			setErrorMessage(null);
			if (Viewers.getFirstSelected(table) == null) {
				setPageComplete(false);
				return;
			}
			setPageComplete(true);
		}

		private void err(String msg) {
			setPageComplete(false);
			setErrorMessage(msg);
		}
	}

	private static class TableLabel extends LabelProvider
		implements ITableLabelProvider {

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
				case 1 -> blockLabelOf(plant);
				case 2 -> Num.str(BiogasPlants.totalElectricPower(plant)) + " kW el.";
				default -> null;
			};
		}

		private String blockLabelOf(BiogasPlant plant) {
			if (plant == null) return "";
			var boilers = plant.boilers;
			if (boilers.size() != 1)
				return boilers.size() + " Blöcke";
			var first = plant.boilers.getFirst();
			return first != null && first.boiler != null
				? first.boiler.name
				: "";
		}
	}
}
