package sophena.rcp;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import sophena.rcp.editors.StartPage;

public class WindowAdvisor extends WorkbenchWindowAdvisor {

	public WindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	@Override
	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new ActionBarAdvisor(configurer);
	}

	@Override
	public void preWindowOpen() {
		var config = getWindowConfigurer();
		config.setInitialSize(new Point(800, 600));
		config.setShowCoolBar(true);
		config.setShowStatusLine(true);
		config.setShowProgressIndicator(true);
		config.setShowMenuBar(true);
		config.setTitle("Sophena " + App.version());
	}

	@Override
	public void postWindowOpen() {
		StartPage.open();
		StartPage.open();
	}

	@Override
	public boolean preWindowShellClose() {
		return super.preWindowShellClose();
	}

	@Override
	public void postWindowClose() {
		ImageManager.dispose();
	}

}
