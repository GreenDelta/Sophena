package sophena.rcp.wizards;

import java.util.List;
import java.util.UUID;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ProductGroupDao;
import sophena.db.daos.ProjectDao;
import sophena.model.Producer;
import sophena.model.ProductCosts;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.Labels;
import sophena.rcp.M;
import sophena.rcp.editors.producers.ProducerEditor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

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
		dialog.setPageSize(150, 400);
		if (dialog.open() == Window.OK)
			Navigator.refresh();
	}

	private static Producer initProducer(Project project) {
		Producer p = new Producer();
		int i = 1;
		for (Producer other : project.producers) {
			if (other.hasProfile)
				i++;
		}
		p.id = UUID.randomUUID().toString();
		p.name = "Erzeugerlastgang " + i;
		p.hasProfile = true;
		p.rank = Wizards.nextProducerRank(project);
		p.costs = new ProductCosts();
		return p;
	}

	@Override
	public boolean performFinish() {
		try {
			int groupIdx = page.groupCombo.getSelectionIndex();
			ProductGroup[] groups = page.productGroups;
			if (groups != null && groups.length > groupIdx) {
				ProductGroup group = groups[groupIdx];
				ProductCosts.copy(group, producer.costs);
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

		private Page() {
			super("ProducerProfilePage", "Erzeugerlastgang integrieren", null);
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
					null, // waste heat
					ProductType.COGENERATION_PLANT,
					ProductType.SOLAR_THERMAL_PLANT,
					ProductType.ELECTRIC_HEAT_GENERATOR,
					ProductType.BIOMASS_BOILER,
					ProductType.FOSSIL_FUEL_BOILER
			};
			String[] items = new String[types.length];
			for (int i = 0; i < types.length; i++) {
				if (types[i] == null)
					items[i] = "Abwärme";
				else
					items[i] = Labels.get(types[i]);
			}
			combo.setItems(items);
			combo.select(0);
			Controls.onSelect(combo, e -> {
				int i = combo.getSelectionIndex();
				this.productType = types[i];
				updateGroupCombo();
			});
		}

		private void updateGroupCombo() {
			if (productType == null) {
				productGroups = new ProductGroup[] { null };
			} else {
				ProductGroupDao dao = new ProductGroupDao(App.getDb());
				List<ProductGroup> groups = dao.getAll(productType);
				Sorters.productGroups(groups);
				productGroups = new ProductGroup[groups.size() + 1];
				int i = 1;
				for (ProductGroup group : groups) {
					productGroups[i] = group;
					i++;
				}
			}
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
			Combo function = UI.formCombo(composite, "Funktion");
			Wizards.fillProducerFunctions(project, function);
			producer.function = Wizards.getProducerFunction(function);
			Controls.onSelect(function, e -> {
				producer.function = Wizards.getProducerFunction(function);
			});
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
