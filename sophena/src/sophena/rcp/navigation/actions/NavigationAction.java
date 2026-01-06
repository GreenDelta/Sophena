package sophena.rcp.navigation.actions;

import java.util.List;

import org.eclipse.jface.action.Action;

import sophena.rcp.navigation.NavigationElement;

public abstract class NavigationAction extends Action {

	public abstract boolean accept(List<NavigationElement> elements);

}
