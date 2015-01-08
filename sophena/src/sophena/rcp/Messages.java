package sophena.rcp;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String File;
	public static String Help;
	public static String ShowViews;
	public static String Window;

	static {
		NLS.initializeMessages("sophena.rcp.messages", Messages.class);
	}

	private Messages() {
	}
}
