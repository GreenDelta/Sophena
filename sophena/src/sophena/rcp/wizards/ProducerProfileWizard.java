package sophena.rcp.wizards;

import java.io.File;
import java.util.List;
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
import sophena.db.daos.ProductGroupDao;
import sophena.db.daos.ProjectDao;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.ProducerProfile;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.editors.producers.ProducerEditor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.Log;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;
import sophena.utils.Strings;

public class ProducerProfileWizard extends Wizard {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Project project;
	private Producer producer;
	private Page page;

	public static void open(ProjectDescriptor d) {
		if (d == null)
			return;
		ProjectDao dao = new ProjectDao(App.getDb());
		open(dao.get(d.id));
	}

	public static void open(Project project) {
		if (project == null)
			return;
		ProducerProfileWizard wiz = new ProducerProfileWizard();
		wiz.setWindowTitle("Erzeugerlastgang integrieren");
		wiz.project = project;
		wiz.producer = initProducer(project);
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		if (dialog.open() == Window.OK) {
			Navigator.refresh();
		}
	}

	private static Producer initProducer(Project project) {
		Producer p = new Producer();
		int i = 1;
		for (Producer other : project.producers) {
			if (other.hasProfile()) {
				i++;
			}
		}
		p.id = UUID.randomUUID().toString();
		p.name = "Erzeugerlastgang " + i;
		p.utilisationRate = 1.0;
		p.rank = Wizards.nextProducerRank(project);
		return p;
	}

	@Override
	public boolean performFinish() {
		try {
			int groupIdx = page.groupCombo.getSelectionIndex();
			ProductGroup[] groups = page.productGroups;
			if (groups != null && groups.length > groupIdx) {
				producer.productGroup = groups[groupIdx];
				Wizards.initFuelSpec(producer, project);
				Wizards.initCosts(producer);
				Wizards.initElectricity(producer, project);
			}
			project.producers.add(producer);
			ProjectDao dao = new ProjectDao(App.getDb());
			dao.update(project);
			Navigator.refresh();
			ProducerEditor.open(
					project.toDescriptor(),
					producer.toDescriptor());
			return true;
		} catch (Exception e) {
			log.error("failed to update project with new producer", e);
			return false;
		}
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private ProductType productType;
		private ProductGroup[] productGroups;
		private Combo groupCombo;
		private Text fileText;

		private Page() {
			super("ProducerProfilePage", "Erzeugerlastgang integrieren", null);
			setMessage(" ");
			setPageComplete(false);
		}

		@Override
		public void createControl(Composite parent) {
			Composite root = new Composite(parent, SWT.NONE);
			setControl(root);
			UI.gridLayout(root, 1, 5, 5);
			Composite comp = UI.formComposite(root);
			UI.gridData(comp, true, false);
			name(comp);
			typeCombo(comp);
			groupCombo = UI.formCombo(comp, "Produktgruppe");
			updateGroupCombo();
			fileFields(comp);
			functionFields(root);
		}

		private void name(Composite comp) {
			Text t = UI.formText(comp, M.Name);
			Texts.on(t).init(producer.name).required().onChanged(s -> {
				producer.name = s;
				validate();
			});
		}

		private void typeCombo(Composite comp) {
			Combo combo = UI.formCombo(comp, "Produktbereich");
			ProductType[] types = {
					ProductType.OTHER_HEAT_SOURCE,
					ProductType.COGENERATION_PLANT,
					ProductType.SOLAR_THERMAL_PLANT,
					ProductType.ELECTRIC_HEAT_GENERATOR,
					ProductType.BIOMASS_BOILER,
					ProductType.FOSSIL_FUEL_BOILER
			};
			String[] items = new String[types.length];
			for (int i = 0; i < types.length; i++) {
				items[i] = Labels.get(types[i]);
			}
			combo.setItems(items);
			combo.select(0);
			this.productType = ProductType.OTHER_HEAT_SOURCE;
			Controls.onSelect(combo, e -> {
				int i = combo.getSelectionIndex();
				this.productType = types[i];
				updateGroupCombo();
			});
		}

		private void updateGroupCombo() {
			ProductGroupDao dao = new ProductGroupDao(App.getDb());
			List<ProductGroup> groups = dao.getAll(productType);
			Sorters.productGroups(groups);
			productGroups = groups.toArray(new ProductGroup[groups.size()]);
			String[] items = new String[productGroups.length];
			for (int i = 0; i < items.length; i++) {
				if (productGroups[i] == null)
					items[i] = "";
				else
					items[i] = productGroups[i].name;
			}
			groupCombo.setItems(items);
			groupCombo.select(0);
		}

		private void functionFields(Composite root) {
			Composite composite = new Composite(root, SWT.NONE);
			UI.gridLayout(composite, 4);
			UI.gridData(composite, true, false);
			Text rank = UI.formText(composite, "Rang");
			Texts.on(rank).init(producer.rank)
					.integer().required().onChanged(s -> {
						producer.rank = Num.readInt(rank.getText());
						validate();
					});
			// producer profiles are always initialized as `base load`
			Combo function = UI.formCombo(composite, "Funktion");
			Wizards.fillProducerFunctions(project, function);
			function.select(0);
			producer.function = ProducerFunction.BASE_LOAD;
			Controls.onSelect(function, e -> {
				producer.function = Wizards.getProducerFunction(function);
			});
		}

		private void fileFields(Composite comp) {
			UI.formLabel(comp, "Lastgang");
			Composite fileComp = new Composite(comp, SWT.NONE);
			UI.gridData(fileComp, true, false);
			UI.innerGrid(fileComp, 2);
			fileText = new Text(fileComp, SWT.BORDER | SWT.READ_ONLY);
			fileText.setBackground(Colors.getWhite());
			UI.gridData(fileText, true, false);
			Button btn = new Button(fileComp, SWT.NONE);
			btn.setText("Öffnen");
			Controls.onSelect(btn, e -> onSelectFile());
		}

		private void onSelectFile() {
			File f = FileChooser.open("*.csv", "*.txt");
			if (f == null)
				return;
			try {
				producer.profile = ProducerProfile.read(f);
				fileText.setText(f.getAbsolutePath());
				if (producer.profileMaxPower == 0) {
					double max = Stats.max(producer.profile.maxPower);
					producer.profileMaxPower = max;
				}
				setPageComplete(true);
			} catch (Exception e) {
				MsgBox.error("Datei konnte nicht gelesen werden",
						e.getMessage());
				Log.error(this, "Failed to read producer profile " + f, e);
			}
		}

		private boolean validate() {
			if (Strings.nullOrEmpty(producer.name)) {
				return err("Der Name darf nicht leer sein.");
			}
			if (Wizards.producerRankExists(project, producer.rank)) {
				return err("Es besteht bereits ein Wärmeerzeuger mit"
						+ " dem angegebenen Rang.");
			}
			setErrorMessage(null);
			setPageComplete(true);
			return true;
		}

		private boolean err(String msg) {
			setPageComplete(false);
			setErrorMessage(msg);
			return false;
		}
	}

}
