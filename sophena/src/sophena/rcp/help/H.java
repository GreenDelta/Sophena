package sophena.rcp.help;

import org.eclipse.osgi.util.NLS;

public class H extends NLS {

	public static String ElectricityRevenues;
	public static String BoilerAccessories;
	public static String HeatingNetConstruction;
	public static String BoilerHouseTechnology;
	public static String Buildings;
	public static String CoGenPlants;
	public static String SimultaneityFactor;
	public static String HeatingLoad;
	public static String HeatingNetTechnology;
	public static String Planning;
	public static String ElectricityDemandShare;
	public static String PrimaryEnergyFactor;
	public static String SmoothingFactor;

	static {
		NLS.initializeMessages("sophena.rcp.help.messages", H.class);
	}

	private H() {
	}
}