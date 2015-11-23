package sophena.rcp.wizards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.BoilerDao;
import sophena.db.daos.FuelDao;
import sophena.db.daos.ProjectDao;
import sophena.model.Boiler;
import sophena.model.Fuel;
import sophena.model.FuelSpec;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.ProductCosts;
import sophena.model.Project;
import sophena.model.WoodAmountType;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.Labels;
import sophena.rcp.M;
import sophena.rcp.editors.producers.ProducerEditor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Strings;
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
		private Combo fuelCombo;
		private ListViewer boilerList;
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
			fuelCombo(comp);
			boilerList(root);
			functionFields(root);
			data.bindToUI();
		}

		private void nameField(Composite comp) {
			nameText = UI.formText(comp, M.Name);
			nameEdited = false;
			// smart identification if the name was edited by the user
			Texts.on(nameText).required().onChanged((t) -> {
				Boiler b = Viewers.getFirstSelected(boilerList);
				if (b == null) {
					nameEdited = true;
				} else {
					nameEdited = !Strings.nullOrEqual(t, b.name);
				}
			});
		}

		private void fuelCombo(Composite comp) {
			fuelCombo = UI.formCombo(comp, "Brennstoff");
			Controls.onSelect(fuelCombo, (e) -> {
				data.updateBoilers();
				data.suggestName();
			});
		}

		private void boilerList(Composite root) {
			Composite composite = new Composite(root, SWT.NONE);
			UI.gridData(composite, true, true);
			UI.gridLayout(composite, 1);
			List list = new List(composite, SWT.BORDER);
			boilerList = new ListViewer(list);
			boilerList.setContentProvider(ArrayContentProvider.getInstance());
			boilerList.setLabelProvider(new BoilerLabel());
			boilerList.addSelectionChangedListener((e) -> {
				data.suggestName();
				data.validate();
			});
			UI.gridData(list, true, true);
		}

		private void functionFields(Composite root) {
			Composite composite = new Composite(root, SWT.NONE);
			UI.gridLayout(composite, 4);
			UI.gridData(composite, true, false);
			rankText = UI.formText(composite, "Rang");
			Texts.on(rankText).integer().required().validate(data::validate);
			functionCombo = UI.formCombo(composite, "Funktion");
		}

		private class BoilerLabel extends LabelProvider {
			@Override
			public String getText(Object element) {
				if (!(element instanceof Boiler))
					return null;
				Boiler boiler = (Boiler) element;
				String label = boiler.name + " ("
						+ Num.str(boiler.minPower) + " kW - "
						+ Num.str(boiler.maxPower) + " kW)";
				return label;
			}
		}

		private class DataBinding {

			private void bindToModel(Producer producer) {
				if (producer == null)
					return;
				Boiler b = Viewers.getFirstSelected(boilerList);
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
				fuelCombo.setItems(getFuelItems());
				fuelCombo.select(0);
				Texts.set(rankText, getNextRank());
				updateBoilers();
				fillFunctionCombo();
				setPageComplete(false);
			}

			private void suggestName() {
				if (nameEdited && !Texts.isEmpty(nameText))
					return;
				Boiler b = Viewers.getFirstSelected(boilerList);
				if (b == null)
					nameText.setText("");
				else
					Texts.set(nameText, b.name);
			}

			private String[] getFuelItems() {
				java.util.List<String> list = new ArrayList<>();
				list.add(Labels.get(WoodAmountType.CHIPS));
				list.add(Labels.get(WoodAmountType.LOGS));
				FuelDao dao = new FuelDao(App.getDb());
				for (Fuel fuel : dao.getAll()) {
					if (!fuel.wood)
						list.add(fuel.name);
				}
				Collections.sort(list);
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
					if (matchSelection(b))
						input.add(b);
				}
				input.sort((b1, b2) -> Strings.compare(b1.name,
						b2.name));
				boilerList.setInput(input);
				setPageComplete(false);
			}

			private boolean matchSelection(Boiler b) {
				if (b == null)
					return false;
				int idx = fuelCombo.getSelectionIndex();
				String fuel = fuelCombo.getItem(idx);
				if (b.fuel != null)
					return Strings.nullOrEqual(fuel, b.fuel.name);
				if (b.woodAmountType != null)
					return Strings.nullOrEqual(fuel,
							Labels.get(b.woodAmountType));
				else
					return false;
			}

			private boolean validate() {
				if (!Texts.hasNumber(rankText)) {
					setPageComplete(false);
					setErrorMessage("Der Rang muss ein numerischer Wert sein");
					return false;
				}
				setErrorMessage(null);
				if (Viewers.getFirstSelected(boilerList) == null) {
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
