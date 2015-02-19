package sophena.rcp.navigation;

import org.eclipse.ui.navigator.CommonNavigator;

public class Navigator extends CommonNavigator {

	private NavigationRoot root;

	@Override
	protected Object getInitialInput() {
		root = new NavigationRoot();
		return root;
	}

	public NavigationRoot getRoot() {
		return root;
	}

}
