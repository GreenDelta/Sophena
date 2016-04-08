package sophena.rcp;

import java.io.File;
import java.util.Optional;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.IActionBarConfigurer;

import sophena.io.datapack.Import;
import sophena.rcp.editors.basedata.ProductGroupEditor;
import sophena.rcp.editors.basedata.boilers.BoilerEditor;
import sophena.rcp.editors.basedata.boilers.CoGenPlantEditor;
import sophena.rcp.editors.basedata.buffers.BufferTankEditor;
import sophena.rcp.editors.basedata.buildings.BuildingStateEditor;
import sophena.rcp.editors.basedata.climate.ClimateDataEditor;
import sophena.rcp.editors.basedata.costs.BaseCostEditor;
import sophena.rcp.editors.basedata.fuels.FuelEditor;
import sophena.rcp.editors.basedata.pipes.PipeEditor;
import sophena.rcp.editors.basedata.products.BoilerAccessoriesEditor;
import sophena.rcp.editors.basedata.products.BoilerHouseTechnologyEditor;
import sophena.rcp.editors.basedata.products.BuildingProductEditor;
import sophena.rcp.editors.basedata.products.FlueGasCleaningEditor;
import sophena.rcp.editors.basedata.products.HeatRecoveryEditor;
import sophena.rcp.editors.basedata.products.HeatingNetConstructionEditor;
import sophena.rcp.editors.basedata.products.PlanningEditor;
import sophena.rcp.editors.results.compare.ComparisonDialog;
import sophena.rcp.editors.sql.SqlEditor;
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
		IToolBarManager toolbar = new ToolBarManager(SWT.FLAT | SWT.LEFT);
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
		m.add(Actions.create("Heizkessel", Icon.BOILER_16.des(),
				BoilerEditor::open));
		m.add(Actions.create("KWK-Anlagen", Icon.CO_GEN_16.des(),
				CoGenPlantEditor::open));
		m.add(Actions.create("Wärmeleitungen", Icon.PIPE_16.des(),
				PipeEditor::open));
		m.add(Actions.create("Pufferspeicher", Icon.BUFFER_16.des(),
				BufferTankEditor::open));
		m.add(Actions.create("Kesselzubehör", BoilerAccessoriesEditor::open));
		m.add(Actions.create("Wärmerückgewinnung", HeatRecoveryEditor::open));
		m.add(Actions.create("Rauchgasreinigung", FlueGasCleaningEditor::open));
		m.add(Actions.create("Heizhaus-Technik",
				BoilerHouseTechnologyEditor::open));
		m.add(Actions.create("Gebäude", BuildingProductEditor::open));
		m.add(Actions.create("Wärmenetz-Bau",
				HeatingNetConstructionEditor::open));
		m.add(Actions.create("Planung", PlanningEditor::open));
	}

	private void fillHelpMenu(IMenuManager menu) {
		MenuManager m = new MenuManager(M.Help);
		m.add(aboutAction);
		m.add(Actions.create("SQL", SqlEditor::open));
		menu.add(m);
	}

	private void fillFileMenu(IMenuManager menuBar) {
		MenuManager fileMenu = new MenuManager(M.File,
				IWorkbenchActionConstants.M_FILE);
		fileMenu.add(saveAction);
		fileMenu.add(saveAsAction);
		fileMenu.add(saveAllAction);
		fileMenu.add(new Separator());
		fileMenu.add(closeAction);
		fileMenu.add(closeAllAction);
		fileMenu.add(new Separator());
		fileMenu.add(Actions.create("Datenimport", Icon.IMPORT_16.des(),
				this::importFile));
		fileMenu.add(exitAction);
		menuBar.add(fileMenu);
	}

	@Override
	protected void makeActions(final IWorkbenchWindow window) {
		saveAction = ActionFactory.SAVE.create(window);
		saveAsAction = ActionFactory.SAVE_AS.create(window);
		saveAllAction = ActionFactory.SAVE_ALL.create(window);
		closeAction = ActionFactory.CLOSE.create(window);
		closeAllAction = ActionFactory.CLOSE_ALL.create(window);
		exitAction = ActionFactory.QUIT.create(window);
		aboutAction = ActionFactory.ABOUT.create(window);
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
