package sophena.rcp;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

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
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(800, 600));
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setShowProgressIndicator(true);
		configurer.setShowMenuBar(true);
		configurer.setTitle("Sophena");
	}

	@Override
	public void postWindowOpen() {
		// TODO: open start page
	}

	@Override
	public boolean preWindowShellClose() {
		// TODO: Editors.closeAll();
		return super.preWindowShellClose();
	}

}
