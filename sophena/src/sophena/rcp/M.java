package sophena.rcp;

import org.eclipse.osgi.util.NLS;

public class M extends NLS {

	public static String Add;
	public static String Remove;
	public static String Edit;
	public static String File;
	public static String Help;
	public static String ShowViews;
	public static String Window;
	public static String Name;
	public static String ProjectDurationYears;
	public static String Project;
	public static String Description;
	public static String NewProject;
	public static String CreateNewProject;
	public static String Consumer;
	public static String CreateNewConsumer;
	public static String NewConsumer;
	public static String BuildingType;
	public static String BuildingState;
	public static String HeatDistribution;
	public static String HeatUsage;
	public static String Costs;
	public static String Unknown;
	public static String ConsumerInformation;
	public static String NoEmptyNameAllowed;
	public static String ConsumptionData;

	static {
		NLS.initializeMessages("sophena.rcp.messages", M.class);
	}

	private M() {
	}
}
