package sophena.rcp.wizards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
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

import sophena.db.daos.BoilerDao;
import sophena.db.daos.ProductGroupDao;
import sophena.db.daos.ProjectDao;
import sophena.model.Boiler;
import sophena.model.FuelSpec;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.ProductCosts;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.Labels;
import sophena.rcp.M;
import sophena.rcp.editors.producers.ProducerEditor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Num;

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
		dialog.setPageSize(150, 400);
		if (dialog.open() == Window.OK)
			Navigator.refresh();
	}

	@Override
	public boolean performFinish() {
		try {
			Producer producer = new Producer();
			producer.id = UUID.randomUUID().toString();
			page.data.bindToModel(producer);
			addFuelSpec(producer);
			addCosts(producer);
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

	private void addFuelSpec(Producer producer) {
		FuelSpec spec = new FuelSpec();
		producer.fuelSpec = spec;
		spec.taxRate = (double) 19;
		spec.waterContent = (double) 20;
	}

	private void addCosts(Producer producer) {
		ProductCosts costs = new ProductCosts();
		producer.costs = costs;
		Boiler b = producer.boiler;
		if (b == null)
			return;
		if (b.purchasePrice != null)
			costs.investment = b.purchasePrice;
		ProductCosts.copy(b.group, costs);
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private DataBinding data = new DataBinding();

		private Text nameText;
		private boolean nameEdited;
		private Combo groupCombo;
		private TableViewer boilerTable;
		private Text rankText;
		private Combo functionCombo;

		private Page() {
			super("ProducerWizardPage", M.CreateNewProducer, null);
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
			boilerTable(root);
			functionFields(root);
			data.bindToUI();
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
			Controls.onSelect(groupCombo, e -> {
				data.updateBoilers();
				data.suggestName();
			});
		}

		private void boilerTable(Composite root) {
			Composite composite = new Composite(root, SWT.NONE);
			UI.gridData(composite, true, true);
			UI.gridLayout(composite, 1);
			boilerTable = Tables.createViewer(composite, "Leistungsbereich",
					"Bezeichnung", "Hersteller");
			Tables.bindColumnWidths(boilerTable, 0.3, 0.4, 0.3);
			boilerTable.setContentProvider(ArrayContentProvider.getInstance());
			boilerTable.setLabelProvider(new BoilerLabel());
			boilerTable.addSelectionChangedListener((e) -> {
				data.suggestName();
				data.validate();
			});
		}

		private void functionFields(Composite root) {
			Composite composite = new Composite(root, SWT.NONE);
			UI.gridLayout(composite, 4);
			UI.gridData(composite, true, false);
			rankText = UI.formText(composite, "Rang");
			Texts.on(rankText).integer().required().validate(data::validate);
			functionCombo = UI.formCombo(composite, "Funktion");
		}

		private class BoilerLabel extends LabelProvider
				implements ITableLabelProvider {

			@Override
			public Image getColumnImage(Object elem, int col) {
				return col == 0 ? Icon.BOILER_16.img() : null;
			}

			@Override
			public String getColumnText(Object elem, int col) {
				if (!(elem instanceof Boiler))
					return null;
				Boiler b = (Boiler) elem;
				switch (col) {
				case 0:
					return Num.str(b.minPower) + " - "
							+ Num.str(b.maxPower) + " kW";
				case 1:
					return b.name;
				case 2:
					return b.manufacturer != null ? b.manufacturer.name : null;
				default:
					return null;
				}
			}
		}

		private class DataBinding {

			private void bindToModel(Producer producer) {
				if (producer == null)
					return;
				Boiler b = Viewers.getFirstSelected(boilerTable);
				producer.boiler = b;
				producer.name = nameText.getText();
				producer.rank = Texts.getInt(rankText);
				int fnIdx = functionCombo.getSelectionIndex();
				if (fnIdx == 0)
					producer.function = ProducerFunction.BASE_LOAD;
				else
					producer.function = ProducerFunction.PEAK_LOAD;
			}

			private void bindToUI() {
				String[] groupItems = getGroupItems();
				groupCombo.setItems(groupItems);
				groupCombo.select(groupItems.length > 1 ? 1 : 0);
				Texts.set(rankText, getNextRank());
				updateBoilers();
				fillFunctionCombo();
				setPageComplete(false);
			}

			private void suggestName() {
				if (nameEdited && !Texts.isEmpty(nameText))
					return;
				Boiler b = Viewers.getFirstSelected(boilerTable);
				if (b == null)
					nameText.setText("");
				else
					Texts.set(nameText, b.name);
			}

			private String[] getGroupItems() {
				java.util.List<String> list = new ArrayList<>();
				list.add("");
				ProductGroupDao dao = new ProductGroupDao(App.getDb());
				java.util.List<ProductGroup> groups = dao.getAll();
				Sorters.sort(groups);
				EnumSet<ProductType> types = EnumSet.of(
						ProductType.BIOMASS_BOILER,
						ProductType.FOSSIL_FUEL_BOILER,
						ProductType.COGENERATION_PLANT);
				for (ProductGroup g : groups) {
					if (g.name == null || g.type == null)
						continue;
					if (types.contains(g.type)) {
						list.add(g.name);
					}
				}
				return list.toArray(new String[list.size()]);
			}

			private int getNextRank() {
				Set<Integer> set = new HashSet<>();
				for (Producer p : project.producers)
					set.add(p.rank);
				int next = 1;
				while (set.contains(next))
					next++;
				return next;
			}

			private void fillFunctionCombo() {
				String[] items = new String[2];
				items[0] = Labels.get(ProducerFunction.BASE_LOAD);
				items[1] = Labels.get(ProducerFunction.PEAK_LOAD);
				int selection = 0;
				for (Producer p : project.producers) {
					if (p.function == ProducerFunction.BASE_LOAD) {
						selection = 1;
						break;
					}
				}
				functionCombo.setItems(items);
				functionCombo.select(selection);
			}

			private void updateBoilers() {
				BoilerDao dao = new BoilerDao(App.getDb());
				ArrayList<Boiler> input = new ArrayList<>();
				for (Boiler b : dao.getAll()) {
					if (matchGroup(b)) {
						input.add(b);
					}
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

			private boolean matchGroup(Boiler b) {
				if (b == null)
					return false;
				int idx = groupCombo.getSelectionIndex();
				String group = groupCombo.getItem(idx);
				if (group.equals(""))
					return true;
				if (b.group == null || b.group.name == null)
					return false;
				return Strings.nullOrEqual(group, b.group.name);
			}

			private boolean validate() {
				if (!Texts.hasNumber(rankText)) {
					setPageComplete(false);
					setErrorMessage("Der Rang muss ein numerischer Wert sein");
					return false;
				}
				setErrorMessage(null);
				if (Viewers.getFirstSelected(boilerTable) == null) {
					setPageComplete(false);
					return false;
				} else {
					setPageComplete(true);
					return true;
				}
			}
		}
	}
}
