package sophena.rcp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.ZipUtil;

import sophena.db.Database;
import sophena.rcp.logging.LoggerConfig;

public class Activator extends AbstractUIPlugin {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		Activator.context = bundleContext;
		Workspace.init();
		LoggerConfig.setup();
		log.info("Workspace initialized at {}", Workspace.dir());
		try {
			File dbDir = new File(Workspace.dir(), "database");
			if (!dbDir.exists()) {
				extractDefaultDatabase(Workspace.dir());
			}
			Database db = new Database(dbDir);
			App.init(db);
		} catch (Exception e) {
			log.error("database initialization failed", e);
		}
	}

	private void extractDefaultDatabase(File workspace) {
		try {
			log.info("initialize database @ {}", workspace);
			InputStream zipStream = FileLocator.openStream(getBundle(),
					new Path("resources/database.zip"), false);
			File zipFile = new File(workspace, "@temp.zip");
			try (FileOutputStream out = new FileOutputStream(zipFile)) {
				IOUtils.copy(zipStream, out);
			}
			ZipUtil.unpack(zipFile, workspace);
			if (!zipFile.delete())
				zipFile.deleteOnExit();
		} catch (Exception e) {
			log.error("failed to extract default database", e);
		}
	}

	public void stop(BundleContext bundleContext) {
		try {
			if (App.getDb() != null)
				App.getDb().close();
		} catch (Exception e) {
			log.error("Failed to close database", e);
		}
		Activator.context = null;
	}

}
