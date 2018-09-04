package sophena.rcp.utils;

import java.awt.Desktop.Action;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.utils.Strings;

public class Desktop {

	public static void browse(String uri) {
		if (Strings.nullOrEmpty(uri))
			return;
		try {
			if (java.awt.Desktop.isDesktopSupported()) {
				java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
				if (desktop.isSupported(Action.BROWSE)) {
					desktop.browse(new URI(uri));
				}
			}
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Desktop.class);
			log.error("Browse URI failed: " + uri, e);
		}
	}

}
