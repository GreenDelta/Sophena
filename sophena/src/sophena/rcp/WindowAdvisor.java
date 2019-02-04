package sophena.rcp;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

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
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(800, 600));
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setShowProgressIndicator(true);
		configurer.setShowMenuBar(true);
		configurer.setTitle("Sophena " + getVersion());
	}

	private String getVersion() {
		BundleContext context = Activator.getContext();
		if (context == null || context.getBundle() == null)
			return "";
		Version v = context.getBundle().getVersion();
		if (v == null)
			return "";
		String s = v.toString();
		if (s.endsWith(".0"))
			return s.substring(0, s.length() - 2);
		return s;
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
