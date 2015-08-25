package sophena.rcp;

import java.io.File;

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
import sophena.rcp.editors.basedata.boilers.BoilerEditor;
import sophena.rcp.editors.basedata.buffers.BufferTankEditor;
import sophena.rcp.editors.basedata.climate.ClimateDataEditor;
import sophena.rcp.editors.basedata.costs.BaseCostEditor;
import sophena.rcp.editors.basedata.fuels.FuelEditor;
import sophena.rcp.editors.basedata.pipes.PipeEditor;
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
		fillHelpMenu(menu);
	}

	private void fillProjectMenu(IMenuManager menu) {
		MenuManager projectMenu = new MenuManager(M.Project);
		Action newAction = Actions.create(M.NewProject,
				Images.NEW_PROJECT_16.des(), ProjectWizard::open);
		projectMenu.add(newAction);
		menu.add(projectMenu);
	}

	private void fillBaseDataMenu(IMenuManager menu) {
		MenuManager m = new MenuManager(M.BaseData);
		m.add(Actions.create(M.ClimateData, Images.CLIMATE_16.des(),
				ClimateDataEditor::open));
		m.add(Actions.create(M.Fuels, Images.FUEL_16.des(),
				FuelEditor::open));
		m.add(Actions.create("Heizkessel", Images.BOILER_16.des(),
				BoilerEditor::open));
		m.add(Actions.create("Wärmeleitungen", Images.PIPE_16.des(),
				PipeEditor::open));
		m.add(Actions.create("Pufferspeicher", Images.BUFFER_16.des(),
				BufferTankEditor::open));
		m.add(Actions.create("Kosteneinstellungen", Images.COSTS_16.des(),
				BaseCostEditor::open));
		menu.add(m);
	}

	private void fillHelpMenu(IMenuManager menuBar) {
		MenuManager helpMenu = new MenuManager(M.Help);
		helpMenu.add(aboutAction);
		menuBar.add(helpMenu);
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
		fileMenu.add(Actions.create("Datenimport", Images.IMPORT_16.des(),
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
			Rcp.run("Importiere Daten ...", in);
		} catch (Exception e) {
			MsgBox.error("Datei konnte nicht gelesen werden");
		}
	}
}
