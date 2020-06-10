package sophena.rcp;

import java.io.File;
import java.util.Optional;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.IActionBarConfigurer;

import sophena.Labels;
import sophena.io.datapack.Import;
import sophena.model.ProductType;
import sophena.rcp.editors.basedata.BaseCostEditor;
import sophena.rcp.editors.basedata.ProductGroupEditor;
import sophena.rcp.editors.basedata.buildings.BuildingStateEditor;
import sophena.rcp.editors.basedata.climate.ClimateDataEditor;
import sophena.rcp.editors.basedata.fuels.FuelEditor;
import sophena.rcp.editors.basedata.manufacturers.ManufacturerEditor;
import sophena.rcp.editors.basedata.products.ProductEditor;
import sophena.rcp.editors.results.compare.ComparisonDialog;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Rcp;
import sophena.rcp.utils.UI;
import sophena.rcp.wizards.ProjectWizard;

public class ActionBarAdvisor extends
		org.eclipse.ui.application.ActionBarAdvisor {

	private IWorkbenchAction aboutAction;
	private IWorkbenchAction closeAction;
	private IWorkbenchAction closeAllAction;
	private IWorkbenchAction exitAction;

	private IWorkbenchAction saveAction;
	private IWorkbenchAction saveAllAction;
	private IWorkbenchAction saveAsAction;

	public ActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {
		var toolbar = new ToolBarManager(SWT.FLAT | SWT.LEFT);
		coolBar.add(new ToolBarContributionItem(toolbar, "main"));
		toolbar.add(saveAction);
		toolbar.add(saveAsAction);
		toolbar.add(saveAllAction);
	}

	@Override
	protected void fillMenuBar(IMenuManager menu) {
		super.fillMenuBar(menu);
		fillFileMenu(menu);
		fillProjectMenu(menu);
		fillBaseDataMenu(menu);
		fillProductMenu(menu);
		fillHelpMenu(menu);
	}

	private void fillProjectMenu(IMenuManager menu) {
		MenuManager projectMenu = new MenuManager(M.Project);
		Action newAction = Actions.create(M.NewProject,
				Icon.NEW_PROJECT_16.des(), ProjectWizard::open);
		projectMenu.add(newAction);
		Action compare = Actions.create("Projekte vergleichen",
				Icon.BAR_CHART_16.des(),
				() -> ComparisonDialog.open(Optional.empty()));
		projectMenu.add(compare);
		menu.add(projectMenu);
	}

	private void fillBaseDataMenu(IMenuManager menu) {
		MenuManager m = new MenuManager(M.BaseData);
		m.add(Actions.create(M.ClimateData, Icon.CLIMATE_16.des(),
				ClimateDataEditor::open));
		m.add(Actions.create("Gebäudetypen", Icon.BUILDING_TYPE_16.des(),
				BuildingStateEditor::open));
		m.add(Actions.create(M.Fuels, Icon.FUEL_16.des(),
				FuelEditor::open));
		m.add(Actions.create("Produktgruppen", Icon.PRODUCT_16.des(),
				ProductGroupEditor::open));
		m.add(Actions.create("Kosteneinstellungen", Icon.COSTS_16.des(),
				BaseCostEditor::open));
		menu.add(m);
	}

	private void fillProductMenu(IMenuManager menu) {
		MenuManager m = new MenuManager("Produktdaten");
		menu.add(m);
		for (ProductType type : ProductType.values()) {
			m.add(Actions.create(
					Labels.getPlural(type),
					img(type),
					() -> ProductEditor.open(type)));
		}
		m.add(Actions.create(
				M.Manufacturers,
				Icon.MANUFACTURER_16.des(),
				ManufacturerEditor::open));
		m.add(Actions.create(
				"Produktdatenbank zurücksetzen",
				Icon.DELETE_16.des(),
				new ProductCleanup()));
	}

	private ImageDescriptor img(ProductType type) {
		if (type == null)
			return null;
		switch (type) {
		case BIOMASS_BOILER:
		case FOSSIL_FUEL_BOILER:
			return Icon.BOILER_16.des();
		case HEAT_PUMP:
			return Icon.HEAT_PUMP_16.des();
		case COGENERATION_PLANT:
			return Icon.CO_GEN_16.des();
		case HEAT_RECOVERY:
			return Icon.HEAT_RECOVERY_16.des();
		case FLUE_GAS_CLEANING:
			return Icon.FLUE_GAS_16.des();
		case BUFFER_TANK:
			return Icon.BUFFER_16.des();
		case PIPE:
			return Icon.PIPE_16.des();
		case TRANSFER_STATION:
			return Icon.CONSUMER_16.des();
		default:
			return null;
		}
	}

	private void fillHelpMenu(IMenuManager menu) {
		MenuManager m = new MenuManager(M.Help);
		m.add(aboutAction);
		// SQL query editor
		// m.add(Actions.create("SQL", SqlEditor::open));
		menu.add(m);
	}

	private void fillFileMenu(IMenuManager menuBar) {
		MenuManager menu = new MenuManager(M.File,
				IWorkbenchActionConstants.M_FILE);
		menu.add(saveAction);
		menu.add(saveAsAction);
		menu.add(saveAllAction);
		menu.add(new Separator());
		menu.add(closeAction);
		menu.add(closeAllAction);
		menu.add(new Separator());
		menu.add(Actions.create("Datenimport", Icon.IMPORT_16.des(),
				this::importFile));
		createWorkspaceActions(menu);
		menu.add(new Separator());
		menu.add(exitAction);
		menuBar.add(menu);
	}

	private void createWorkspaceActions(MenuManager parent) {
		AppConfig conf = AppConfig.load();
		if (conf.lastDataDirs.isEmpty()) {
			parent.add(Actions.create("Datenverzeichnis wechseln",
					Workspace::switchWorkspace));
			return;
		}
		MenuManager menu = new MenuManager("Datenverzeichnis wechseln");
		parent.add(menu);
		for (String dir : conf.lastDataDirs) {
			Action a = Actions.create(dir,
					() -> Workspace.switchWorkspace(dir));
			menu.add(a);
		}
		menu.add(Actions.create("Anderes ...", Workspace::switchWorkspace));
	}

	@Override
	protected void makeActions(final IWorkbenchWindow window) {
		saveAction = ActionFactory.SAVE.create(window);
		saveAction.setText("Speichern");
		saveAsAction = ActionFactory.SAVE_AS.create(window);
		saveAsAction.setText("Speichern unter...");
		saveAllAction = ActionFactory.SAVE_ALL.create(window);
		saveAllAction.setText("Alles speichern");
		closeAction = ActionFactory.CLOSE.create(window);
		closeAction.setText("Schließen");
		closeAllAction = ActionFactory.CLOSE_ALL.create(window);
		closeAllAction.setText("Alles schließen");
		exitAction = ActionFactory.QUIT.create(window);
		exitAction.setText("Sophena beenden");
		aboutAction = ActionFactory.ABOUT.create(window);
		aboutAction.setText("Über Sophena");
	}

	private void importFile() {
		FileDialog dialog = new FileDialog(UI.shell(), SWT.OPEN);
		dialog.setFilterExtensions(new String[] { "*.sophena" });
		dialog.setText(M.SelectFile);
		String path = dialog.open();
		if (path == null)
			return;
		File file = new File(path);
		try {
			Import in = new Import(file, App.getDb());
			Rcp.run("Importiere Daten ...", in, () -> Navigator.refresh());
		} catch (Exception e) {
			MsgBox.error("Datei konnte nicht gelesen werden");
		}
	}
}
