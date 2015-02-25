package sophena.rcp;

import java.io.File;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.Database;
import sophena.rcp.utils.Rcp;

public class Activator extends AbstractUIPlugin {

	private Logger log = LoggerFactory.getLogger(getClass());

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		// TODO: add log file etc
		org.apache.log4j.Logger log = org.apache.log4j.Logger.getRootLogger();
		log.setLevel(Level.ALL);
		log.addAppender(new ConsoleAppender(new PatternLayout(
				"%-4r %-5p %c %x - %m%n")));
		try {
			File workspace = Rcp.getWorkspace();
			File dbDir = new File(workspace, "database");
			Database db = new Database(dbDir);
			App.init(db);
		} catch (Exception e) {
			log.error("database initialization failed", e);
		}
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
