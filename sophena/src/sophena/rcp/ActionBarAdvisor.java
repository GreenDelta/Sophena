package sophena.rcp;

import java.io.File;
import java.util.Optional;

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
import sophena.rcp.colors.ColorConfigDialog;
import sophena.rcp.editors.basedata.BaseCostEditor;
import sophena.rcp.editors.basedata.ProductGroupEditor;
import sophena.rcp.editors.basedata.buildings.BuildingStateEditor;
import sophena.rcp.editors.basedata.climate.ClimateDataEditor;
import sophena.rcp.editors.basedata.fuels.FuelEditor;
import sophena.rcp.editors.basedata.manufacturers.ManufacturerEditor;
import sophena.rcp.editors.basedata.products.ProductEditor;
import sophena.rcp.editors.biogas.substrate.SubstrateEditor;
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

	public ActionBarAdvisor(IActionBarConfigurer config) {
		super(config);
	}

	@Override
	protected void fillCoolBar(ICoolBarManager top) {
		var toolbar = new ToolBarManager(SWT.FLAT | SWT.LEFT);
		top.add(new ToolBarContributionItem(toolbar, "main"));
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
		fillBiogasMenu(menu);
		fillHelpMenu(menu);
	}

	private void fillProjectMenu(IMenuManager man) {
		var m = new MenuManager(M.Project);
		man.add(m);
		m.add(Actions.create(M.NewProject, Icon.NEW_PROJECT_16.des(),
				ProjectWizard::open));
		m.add(Actions.create("Projekte vergleichen", Icon.BAR_CHART_16.des(),
				() -> ComparisonDialog.open(Optional.empty())));
	}

	private void fillBaseDataMenu(IMenuManager man) {
		var m = new MenuManager(M.BaseData);
		man.add(m);
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
	}

	private void fillProductMenu(IMenuManager man) {
		var m = new MenuManager("Produktdaten");
		man.add(m);
		for (var type : ProductType.values()) {
			if (type == ProductType.ELECTRIC_HEAT_GENERATOR
					|| type == ProductType.OTHER_HEAT_SOURCE)
				continue;
			m.add(Actions.create(Labels.getPlural(type), img(type),
					() -> ProductEditor.open(type)));
		}
		m.add(Actions.create(M.Manufacturers, Icon.MANUFACTURER_16.des(),
				ManufacturerEditor::open));
		m.add(Actions.create(
				"Produktdatenbank zurücksetzen", Icon.DELETE_16.des(),
				new ProductCleanup()));
	}

	private void fillBiogasMenu(IMenuManager man) {
		var m = new MenuManager("Biogasanlagenkonfigurator");
		man.add(m);
		m.add(Actions.create("Biogasanlagen", () -> {}));
		m.add(Actions.create("Substrate", SubstrateEditor::open));
		m.add(Actions.create("Strompreise", () -> {}));
		m.add(Actions.create("Marktwerte", () -> {}));
	}

	private ImageDescriptor img(ProductType type) {
		if (type == null)
			return null;
		return switch (type) {
			case BIOMASS_BOILER, FOSSIL_FUEL_BOILER -> Icon.BOILER_16.des();
			case HEAT_PUMP -> Icon.HEAT_PUMP_16.des();
			case COGENERATION_PLANT -> Icon.CO_GEN_16.des();
			case HEAT_RECOVERY -> Icon.HEAT_RECOVERY_16.des();
			case FLUE_GAS_CLEANING -> Icon.FLUE_GAS_16.des();
			case BUFFER_TANK -> Icon.BUFFER_16.des();
			case PIPE -> Icon.PIPE_16.des();
			case TRANSFER_STATION -> Icon.CONSUMER_16.des();
			case SOLAR_THERMAL_PLANT -> Icon.SOLARTHERM_16.des();
			default -> null;
		};
	}

	private void fillHelpMenu(IMenuManager man) {
		var m = new MenuManager(M.Help);
		man.add(m);
		m.add(Actions.create("Ergebnisfarben ...", ColorConfigDialog::show));
		m.add(aboutAction);
	}

	private void fillFileMenu(IMenuManager man) {
		var m = new MenuManager(M.File, IWorkbenchActionConstants.M_FILE);
		man.add(m);
		m.add(saveAction);
		m.add(saveAsAction);
		m.add(saveAllAction);
		m.add(new Separator());
		m.add(closeAction);
		m.add(closeAllAction);
		m.add(new Separator());
		m.add(Actions.create("Datenimport", Icon.IMPORT_16.des(),
				this::importFile));
		createWorkspaceActions(m);
		m.add(new Separator());
		m.add(exitAction);
	}

	private void createWorkspaceActions(MenuManager top) {
		var conf = AppConfig.load();
		if (conf.lastDataDirs.isEmpty()) {
			top.add(Actions.create("Datenverzeichnis wechseln",
					Workspace::switchWorkspace));
			return;
		}
		var sub = new MenuManager("Datenverzeichnis wechseln");
		top.add(sub);
		for (var dir : conf.lastDataDirs) {
			sub.add(Actions.create(dir, () -> Workspace.switchWorkspace(dir)));
		}
		sub.add(Actions.create("Anderes ...", Workspace::switchWorkspace));
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
		var dialog = new FileDialog(UI.shell(), SWT.OPEN);
		dialog.setFilterExtensions(new String[]{"*.sophena"});
		dialog.setText(M.SelectFile);
		String path = dialog.open();
		if (path == null)
			return;
		var file = new File(path);
		try {
			var in = new Import(file, App.getDb());
			Rcp.run("Importiere Daten ...", in, Navigator::refresh);
		} catch (Exception e) {
			MsgBox.error("Datei konnte nicht gelesen werden");
		}
	}
}
