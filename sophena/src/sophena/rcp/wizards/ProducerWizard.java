package sophena.rcp.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
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

import sophena.db.daos.BoilerDao;
import sophena.db.daos.FuelDao;
import sophena.db.daos.ProductGroupDao;
import sophena.db.daos.ProjectDao;
import sophena.model.Boiler;
import sophena.model.FuelGroup;
import sophena.model.Producer;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.editors.producers.ProducerEditor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Strings;

public class ProducerWizard extends Wizard {

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
		ProducerWizard wiz = new ProducerWizard();
		wiz.setWindowTitle(M.CreateNewProducer);
		wiz.project = project;
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		dialog.setPageSize(150, 450);
		if (dialog.open() == Window.OK) {
			Navigator.refresh();
		}
	}

	@Override
	public boolean performFinish() {
		try {
			Producer producer = new Producer();
			producer.id = UUID.randomUUID().toString();
			page.bindToModel(producer);
			Wizards.initFuelSpec(producer);
			Wizards.initCosts(producer);
			ProductGroup pg = producer.productGroup;
			if (pg != null && pg.type == ProductType.COGENERATION_PLANT) {
				producer.producedElectricity = new FuelDao(App.getDb())
						.getAll().stream()
						.filter(e -> e.group == FuelGroup.ELECTRICITY)
						.findFirst().orElse(null);
			}

			project.producers.add(producer);
			ProjectDao dao = new ProjectDao(App.getDb());
			dao.update(project);
			Navigator.refresh();
			ProducerEditor
					.open(project.toDescriptor(), producer.toDescriptor());
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

		private Text nameText;
		private boolean nameEdited;
		private Combo groupCombo;
		private TableViewer boilerTable;
		private Text rankText;
		private Combo functionCombo;

		private Combo powerCombo;
		private PowerFilter powerFilter;
		private ProductGroup[] groupFilter;

		private Page() {
			super("ProducerWizardPage", M.CreateNewProducer, null);
			setMessage(" ");
			this.groupFilter = getGroups();
		}

		private ProductGroup[] getGroups() {
			List<ProductGroup> list = new ArrayList<>();
			list.add(null);
			ProductGroupDao dao = new ProductGroupDao(App.getDb());
			java.util.List<ProductGroup> groups = dao.getAll();
			Sorters.productGroups(groups);
			EnumSet<ProductType> types = EnumSet.of(
					ProductType.BIOMASS_BOILER,
					ProductType.FOSSIL_FUEL_BOILER,
					ProductType.HEAT_PUMP,
					ProductType.COGENERATION_PLANT);
			for (ProductGroup g : groups) {
				if (g.name == null || g.type == null)
					continue;
				if (types.contains(g.type)) {
					list.add(g);
				}
			}
			return list.toArray(new ProductGroup[list.size()]);
		}

		@Override
		public void createControl(Composite parent) {
			Composite root = new Composite(parent, SWT.NONE);
			setControl(root);
			UI.gridLayout(root, 1, 5, 5);
			Composite comp = UI.formComposite(root);
			UI.gridData(comp, true, false);
			nameField(comp);
			groupCombo(comp);
			powerCombo(comp);
			boilerTable(root);
			functionFields(root);
			bindToUI();
		}

		private void nameField(Composite comp) {
			nameText = UI.formText(comp, M.Name);
			nameEdited = false;
			// smart identification if the name was edited by the user
			Texts.on(nameText).required().onChanged((t) -> {
				Boiler b = Viewers.getFirstSelected(boilerTable);
				if (b == null) {
					nameEdited = true;
				} else {
					nameEdited = !Strings.nullOrEqual(t, b.name);
				}
			});
		}

		private void groupCombo(Composite comp) {
			groupCombo = UI.formCombo(comp, "Produktgruppe");
			String[] items = Arrays.stream(groupFilter)
					.map(g -> g == null ? "" : g.name)
					.toArray(String[]::new);
			groupCombo.setItems(items);
			groupCombo.select(items.length > 1 ? 1 : 0);
			Controls.onSelect(groupCombo, e -> {
				updatePowerFilter();
				updateBoilers();
				suggestName();
			});
		}

		private void updatePowerFilter() {
			PowerFilter filter = PowerFilter.get(getGroup());
			if (filter == null && this.powerFilter == null)
				return;
			if (filter == null && this.powerFilter != null) {
				this.powerFilter = null;
				powerCombo.setItems();
				powerCombo.setEnabled(false);
				return;
			}
			if (this.powerFilter != null
					&& this.powerFilter.type == filter.type)
				return;
			this.powerFilter = filter;
			powerCombo.setEnabled(true);
			powerCombo.setItems(filter.labels);
			powerCombo.select(0);
		}

		private ProductGroup getGroup() {
			int idx = groupCombo.getSelectionIndex();
			if (idx >= 0 && idx < groupFilter.length)
				return groupFilter[idx];
			return null;
		}

		private void powerCombo(Composite comp) {
			powerCombo = UI.formCombo(comp, "Größenklasse");
			updatePowerFilter();
			Controls.onSelect(powerCombo, e -> {
				updateBoilers();
				suggestName();
			});
		}

		private void boilerTable(Composite root) {
			Composite comp = new Composite(root, SWT.NONE);
			UI.gridData(comp, true, true);
			UI.gridLayout(comp, 1);
			boilerTable = Tables.createViewer(comp, "Hersteller",
					"Nennleistung", "Bezeichnung");
			Tables.bindColumnWidths(boilerTable, 0.3, 0.25, 0.45);
			boilerTable.setContentProvider(ArrayContentProvider.getInstance());
			boilerTable.setLabelProvider(new BoilerLabel());
			boilerTable.addSelectionChangedListener((e) -> {
				suggestName();
				validate();
			});
		}

		private void functionFields(Composite root) {
			Composite composite = new Composite(root, SWT.NONE);
			UI.gridLayout(composite, 4);
			UI.gridData(composite, true, false);
			rankText = UI.formText(composite, "Rang");
			Texts.on(rankText).integer().required().validate(this::validate);
			functionCombo = UI.formCombo(composite, "Funktion");
		}

		private void bindToModel(Producer p) {
			if (p == null)
				return;
			Boiler b = Viewers.getFirstSelected(boilerTable);
			p.boiler = b;
			if (b != null) {
				p.productGroup = b.group;
			}
			p.name = nameText.getText();
			p.rank = Texts.getInt(rankText);
			p.function = Wizards.getProducerFunction(functionCombo);
		}

		private void bindToUI() {
			Texts.set(rankText, Wizards.nextProducerRank(project));
			updateBoilers();
			Wizards.fillProducerFunctions(project, functionCombo);
			setPageComplete(false);
		}

		private void suggestName() {
			if (nameEdited && !Texts.isEmpty(nameText))
				return;
			Boiler b = Viewers.getFirstSelected(boilerTable);
			if (b == null) {
				nameText.setText("");
			} else {
				Texts.set(nameText, b.name);
			}
		}

		private void updateBoilers() {
			BoilerDao dao = new BoilerDao(App.getDb());
			ArrayList<Boiler> input = new ArrayList<>();
			ProductGroup group = getGroup();
			for (Boiler b : dao.getAll()) {
				if (group != null && !Objects.equals(b.group, group))
					continue;
				if (!PowerFilter.matches(b, powerFilter,
						powerCombo.getSelectionIndex()))
					continue;
				input.add(b);
			}
			input.sort((b1, b2) -> {
				if (Math.abs(b1.minPower - b2.minPower) > 0.1)
					return Double.compare(b1.minPower, b2.minPower);
				if (Math.abs(b1.maxPower - b2.maxPower) > 0.1)
					return Double.compare(b1.maxPower, b2.maxPower);
				return Strings.compare(b1.name, b2.name);
			});
			boilerTable.setInput(input);
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
			if (Viewers.getFirstSelected(boilerTable) == null) {
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

	private static class PowerFilter {
		final ProductType type;
		final double[][] ranges;
		final String[] labels;

		PowerFilter(ProductType type, double[][] ranges, String[] labels) {
			this.type = type;
			this.ranges = ranges;
			this.labels = labels;
		}

		int len() {
			if (ranges == null || labels == null)
				return 0;
			return Math.min(ranges.length, labels.length);
		}

		static PowerFilter get(ProductGroup group) {
			if (group == null || group.type == null)
				return null;
			switch (group.type) {
			case BIOMASS_BOILER:
			case FOSSIL_FUEL_BOILER:
				return new PowerFilter(group.type,
						new double[][] {
								null,
								{ 0, 100 },
								{ 100, 250 },
								{ 250, 500 },
								{ 500, 1000 },
								{ 1000, Integer.MAX_VALUE }
						},
						new String[] {
								"",
								"bis 100 kW",
								"100 - 250 kW",
								"250 - 500 kW",
								"500 - 1000 kW",
								"über 1 MW" });
			case HEAT_PUMP:
				return new PowerFilter(group.type,
						new double[][] {
								null,
								{ 0, 50 },
								{ 50, 250 },
								{ 250, Integer.MAX_VALUE }
						},
						new String[] {
								"",
								"bis 50 kW",
								"50 - 250 kW",
								"über 250 kW" });
			case COGENERATION_PLANT:
				return new PowerFilter(group.type,
						new double[][] {
								null,
								{ 0, 50 },
								{ 50, 150 },
								{ 150, 500 },
								{ 500, Integer.MAX_VALUE }
						},
						new String[] {
								"",
								"bis 50 kW el.",
								"50 - 150 kW el.",
								"150 - 500 kW el.",
								"über 500 kW el." });
			default:
				return null;
			}
		}

		static boolean matches(Boiler boiler, PowerFilter filter, int i) {
			if (boiler == null)
				return false;
			if (filter == null || i >= filter.len())
				return true;
			double[] range = filter.ranges[i];
			if (boiler.isCoGenPlant)
				return matches(boiler.maxPowerElectric, range);
			return matches(boiler.maxPower, range);
		}

		static boolean matches(double value, double[] range) {
			if (range == null || range.length < 2)
				return true;
			return value >= range[0] && value <= range[1];
		}

	}

}
