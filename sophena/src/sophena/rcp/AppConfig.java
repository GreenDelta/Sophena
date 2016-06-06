package sophena.rcp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class AppConfig {

	public String dataDir;

	/** Loads the application configuration from the user folder. */
	public static AppConfig load() {
		File f = getFile();
		if (f == null || !f.exists())
			return new AppConfig();
		try (InputStream is = new FileInputStream(f);
				Reader reader = new InputStreamReader(is, "utf-8")) {
			Gson gson = new Gson();
			return gson.fromJson(reader, AppConfig.class);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(AppConfig.class);
			log.error("Failed to read configuration file", e);
			return new AppConfig();
		}

	}

	/** Save the configuration to the user folder. */
	public void save() {
		File f = getFile();
		if (f == null)
			return;
		try (OutputStream os = new FileOutputStream(f);
				Writer writer = new OutputStreamWriter(os, "utf-8")) {
			Gson gson = new Gson();
			gson.toJson(this, writer);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(AppConfig.class);
			log.error("Failed to write configuration file", e);
		}
	}

	/**
	 * Deletes the configuration file from the file system and returns true if
	 * the file could be deleted.
	 */
	public static boolean delete() {
		File f = getFile();
		if (f == null || !f.exists())
			return false;
		else
			return f.delete();
	}

	private static File getFile() {
		try {
			File home = new File(System.getProperty("user.home"));
			if (home == null || !home.isDirectory())
				return null;
			File confDir = new File(home, ".sophena");
			if (!confDir.exists())
				confDir.mkdirs();
			return new File(confDir, "config.json");
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(AppConfig.class);
			log.error("Failed to get configuration file", e);
			return null;
		}
	}

}